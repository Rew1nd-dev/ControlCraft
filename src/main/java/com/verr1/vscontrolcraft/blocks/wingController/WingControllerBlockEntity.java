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
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.*;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.OnShipDirectinonalBlockEntity;
import com.verr1.vscontrolcraft.base.UltraTerminal.*;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.WingControllerPeripheral;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WingControllerBlockEntity extends OnShipDirectinonalBlockEntity implements
        IBearingBlockEntity, ITerminalDevice, IPacketHandler, IHaveGoggleInformation
{
    protected ControlledContraptionEntity physicalWing;
    protected LerpedFloat clientAnimatedAngle = LerpedFloat.angular();
    protected float angle;
    protected float adjustSpeed;
    protected boolean running;
    private AssemblyException lastException;

    private ScrollValueBehaviour tiltAngle;

    private WingControllerPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    () -> (double)angle,
                    v -> setAngle(v.floatValue()),
                    "Angle °",
                    WidgetType.SLIDE,
                    ExposedFieldType.DEGREE
            )
    );

    private ExposedFieldWrapper exposedField = fields.get(0);

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
        lazyTickRate = 3;
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
            syncClient(getBlockPos(), level);
        }
    }

    @Override
    public void destroy() {
        if(!level.isClientSide){
            disassemble();
        }
        super.destroy();
    }

    @Override
    public void remove() {
        if(!level.isClientSide){
            disassemble();
        }
        super.remove();
    }

    @Override
    public void tick() {
        super.tick();
        if(physicalWing == null)return;
        applyRotation();
        tickAnimationData();

    }

    @Override
    public void invalidate(){
        super.invalidate();
        if(peripheralCap != null){
            peripheralCap.invalidate();
            peripheralCap = null;
        }
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
        /*
        * tiltAngle = new WingControllerScrollValueBehavior(this)
                .between(-180, 180)
                .withCallback(this::setAngle);
        tiltAngle.setValue(0);
        behaviours.add(tiltAngle);
        * */
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
            var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.SYNC_0)
                    .withDouble(angle)
                    .build();
            AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    public ExposedFieldWrapper getExposedField() {
        return exposedField;
    }

    protected void displayScreen(ServerPlayer player){
        double angle = getAngle();

        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.OPEN_SCREEN_0)
                .withDouble(angle)
                .build();

        AllPackets.sendToPlayer(p, player);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Direction dir = WandRenderer.lookingAtFaceDirection();
        if(dir == null)return true;
        tooltip.add(Components.literal("    Face " + dir + " Bounded:"));
        fields().forEach(f -> {
            if(!f.directionOptional.test(dir))return;
            String info = f.type.getComponent().getString();
            tooltip.add(Component.literal(info).withStyle(ChatFormatting.AQUA));
        });

        return true;
    }

    /*
    @Override
    public void setExposedField(ExposedFieldType type, double min, double max, ExposedFieldDirection openTo) {
        if (type == ExposedFieldType.ANGLE) {
            fields.get(0).min_max.x = min;
            fields.get(0).min_max.y = max;
        }
        exposedField = fields.get(0);
    }
    * */



    @Override
    public String name() {
        return "Wing Controller";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SYNC_0){
            double angle = packet.getDoubles().get(0);
            setAngle((float)angle);
        }
        if(packet.getType() == BlockBoundPacketType.OPEN_SCREEN_0){
            double angle = packet.getDoubles().get(0);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ScreenOpener.open(new WingControllerScreen(packet.getBoundPos(), angle)));
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SETTING_0){
            double angle = packet.getDoubles().get(0);
            setAngle((float)angle);
        }
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if(clientPacket)return;
        fields.forEach(e -> compound.put("field_" + e.type.name(), e.serialize()));
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if(clientPacket)return;
        fields.forEach(e -> e.deserialize(compound.getCompound("field_" + e.type.name())));
        try{
            exposedField = fields.get(compound.getInt("exposed_field"));
        }catch (IndexOutOfBoundsException e){
            exposedField = fields.get(0);
        }


    }
}


/*
* public static class WingControllerScrollValueBehavior extends ScrollValueBehaviour{

        public WingControllerScrollValueBehavior(SmartBlockEntity be) {
            super(Components.translatable(ControlCraft.MODID + ".screen.labels.field.angle"), be, new WingControllerValueBox());
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            ImmutableList<Component> rows = ImmutableList.of(Components.literal("\u27f3")
                            .withStyle(ChatFormatting.BOLD),
                    Components.literal("⟲")
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
                    .add(Lang.text(" °"))
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
* */