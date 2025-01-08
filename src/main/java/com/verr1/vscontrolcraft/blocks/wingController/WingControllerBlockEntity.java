package com.verr1.vscontrolcraft.blocks.wingController;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.bearing.IBearingBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.*;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.IPacketHandle;
import com.verr1.vscontrolcraft.base.SyncAnimationPacket;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.WingControllerPeripheral;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WingControllerBlockEntity extends SmartBlockEntity implements IBearingBlockEntity {
    protected ControlledContraptionEntity physicalWing;
    protected LerpedFloat clientAnimatedAngle;
    protected float angle;
    protected float adjustSpeed;
    protected boolean running;
    private AssemblyException lastException;

    private ScrollValueBehaviour tiltAngle;

    private WingControllerPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new WingControllerPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public WingControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        clientAnimatedAngle = LerpedFloat.angular();
    }

    public void assemble(){
        if (!(level.getBlockState(worldPosition).getBlock() instanceof BearingBlock))
            return;

        Direction direction = getBlockState().getValue(BearingBlock.FACING);
        WingContraption wingContraption = new WingContraption(direction);

        try {
            if (!wingContraption.assemble(level, worldPosition))
                return;

            lastException = null;
        } catch (AssemblyException e) {
            lastException = e;
            ControlCraft.LOGGER.info(e.toString());
            sendData();
            return;
        }

        running = true;
        wingContraption.removeBlocksFromWorld(level, BlockPos.ZERO);
        physicalWing = ControlledContraptionEntity.create(level, this, wingContraption);
        BlockPos anchor = worldPosition.relative(direction);
        physicalWing.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        physicalWing.setRotationAxis(direction.getAxis());
        level.addFreshEntity(physicalWing);

        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(level, worldPosition);
        angle = 0;
        sendData();

    }

    public void disassemble() {
        if (physicalWing == null)
            return;
        angle = 0;
        running = false;
        physicalWing.disassemble();
        AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(level, worldPosition);
        physicalWing = null;
        sendData();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (physicalWing != null && !level.isClientSide){
            sendData();
        }

        if(!level.isClientSide){
            syncClient();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if(physicalWing == null)return;
        applyRotation();
        tickAnimationData();

    }

    protected void applyRotation() {
        float wingAngle = level.isClientSide ? clientAnimatedAngle.getValue() : angle;
        if (physicalWing == null)
            return;
        physicalWing.setAngle(wingAngle);
        BlockState blockState = getBlockState();
        if (blockState.hasProperty(BlockStateProperties.FACING))
            physicalWing.setRotationAxis(
                    blockState
                            .getValue(BlockStateProperties.FACING)
                            .getAxis()
            );
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tiltAngle = new WingControllerScrollValueBehavior(this)
                .between(-180, 180)
                .withCallback(this::setAngle);
        tiltAngle.setValue(0);
        behaviours.add(tiltAngle);
    }

    @Override
    public boolean isAttachedTo(AbstractContraptionEntity contraption) {
        return contraption == physicalWing;
    }

    @Override
    public void attach(ControlledContraptionEntity contraption) {
        BlockState blockState = getBlockState();
        if (!(contraption.getContraption() instanceof BearingContraption))
            return;
        if (!blockState.hasProperty(BearingBlock.FACING))
            return;

        this.physicalWing = contraption;
        setChanged();
        BlockPos anchor = worldPosition.relative(blockState.getValue(BearingBlock.FACING));
        physicalWing.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        if (!level.isClientSide) {
            sendData();
        }
    }

    @Override
    public void onStall() {
        if (!level.isClientSide)
            sendData();
    }

    @Override
    public boolean isValid() {
        return isRemoved();
    }

    @Override
    public BlockPos getBlockPosition() {
        return getBlockPos();
    }

    @Override
    public float getInterpolatedAngle(float partialTicks) {
        return Mth.lerp(partialTicks, angle, angle + adjustSpeed * 0.05f);
    }

    @Override
    public boolean isWoodenTop() {
        return false;
    }

    @Override
    public void setAngle(float forcedAngle) {
        angle = Util.angleReset(forcedAngle);
    }

    public float getAngle() {
        return angle;
    }

    private void tickAnimationData(){
        if(!level.isClientSide)return;
        clientAnimatedAngle.chase(angle, 0.1, LerpedFloat.Chaser.EXP);
        clientAnimatedAngle.tickChaser();
    }



    public void syncClient(){
        if(!level.isClientSide){
            var p = new SyncAnimationPacket<>(this, new WingControllerAnimationDataHandler(angle), WingControllerBlockEntity.class);
            AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    public static class WingControllerAnimationDataHandler implements IPacketHandle<WingControllerBlockEntity>{
        private float animatedAngle;

        public WingControllerAnimationDataHandler(){animatedAngle = 0;}

        public WingControllerAnimationDataHandler(float angle){animatedAngle = angle;}

        @Override
        public void readBuffer(FriendlyByteBuf buffer) {
            animatedAngle = buffer.readFloat();
        }

        @Override
        public void writeBuffer(FriendlyByteBuf buffer) {
            buffer.writeFloat(animatedAngle);
        }

        @Override
        public void handle(WingControllerBlockEntity be) {
            be.setAngle(animatedAngle);
        }
    }

    public static class WingControllerScrollValueBehavior extends ScrollValueBehaviour{

        public WingControllerScrollValueBehavior(SmartBlockEntity be) {
            super(Lang.translateDirect("kinetics.wingbearing.rotated_angle"), be, new WingControllerValueBox());
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            ImmutableList<Component> rows = ImmutableList.of(Components.literal("\u27f3")
                            .withStyle(ChatFormatting.BOLD),
                    Components.literal("\u27f2")
                            .withStyle(ChatFormatting.BOLD));
            return new ValueSettingsBoard(label, 180, 45, rows, new ValueSettingsFormatter(this::formatValue));
        }

        @Override
        public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
            int value = Math.max(1, valueSetting.value());
            if (!valueSetting.equals(getValueSettings()))
                playFeedbackSound(this);
            setValue(valueSetting.row() == 0 ? -value : value);
        }

        @Override
        public ValueSettings getValueSettings() {
            return new ValueSettings(value < 0 ? 0 : 1, Math.abs(value));
        }

        public MutableComponent formatValue(ValueSettings settings) {
            return Lang.number(Math.max(1, Math.abs(settings.value())))
                    .add(Lang.translateDirect("generic.unit.degrees"))
                    .component();
        }

    }


    public static class WingControllerValueBox extends ValueBoxTransform.Sided{

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            Direction facing = state.getValue(WingControllerBlock.FACING);
            if (facing.getAxis() != Direction.Axis.Y && direction == Direction.DOWN)
                return false;
            return direction.getAxis() != facing.getAxis();
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            Direction facing = state.getValue(WingControllerBlock.FACING);
            return super
                    .getLocalOffset(state)
                    .add(Vec3.atLowerCornerOf(facing.getNormal())
                    .scale(-1 / 16f));
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 12.5);
        }

        @Override
        public void rotate(BlockState state, PoseStack ms) {
            super.rotate(state, ms);
            Direction facing = state.getValue(WingControllerBlock.FACING);
            if (facing.getAxis() == Direction.Axis.Y)
                return;
            if (getSide() != Direction.UP)
                return;
            TransformStack.cast(ms)
                    .rotateZ(-AngleHelper.horizontalAngle(facing) + 180);
        }

        @Override
        public boolean testHit(BlockState state, Vec3 localHit) {
            return true;
        }
    }
}
