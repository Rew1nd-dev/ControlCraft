package com.verr1.controlcraft.content.blocks.propeller;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.content.blocks.SharedKeys;
import com.verr1.controlcraft.content.valkyrienskies.attachments.PropellerForceInducer;
import com.verr1.controlcraft.foundation.network.executors.ClientBuffer;
import com.verr1.controlcraft.foundation.network.executors.CompoundTagPort;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.content.cctweaked.peripheral.PropellerControllerPeripheral;
import com.verr1.controlcraft.foundation.api.IPacketHandler;
import com.verr1.controlcraft.foundation.api.delegate.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.SynchronizedField;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.data.logical.LogicalPropeller;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.network.packets.specific.ExposedFieldSyncClientPacket;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.SerializeUtils;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class PropellerControllerBlockEntity extends OnShipBlockEntity implements
        ITerminalDevice, IPacketHandler, IHaveGoggleInformation
{
    public boolean hasAttachedPropeller = false;

    public SynchronizedField<Double> rotationalSpeed = new SynchronizedField<>(0.0);

    public double attachedPropellerThrustRatio = 0;
    public double attachedPropellerTorqueRatio = 0;


    private PropellerControllerPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    rotationalSpeed::read,
                    rotationalSpeed::write,
                    "Speed",
                    ExposedFieldType.SPEED
            ).withSuggestedRange(0, 64),
            new ExposedFieldWrapper(
                    rotationalSpeed::read,
                    rotationalSpeed::write,
                    "Speed",
                    ExposedFieldType.SPEED$1
            ).withSuggestedRange(0, 64)
    );

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new PropellerControllerPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidate(){
        super.invalidate();
        if(peripheralCap != null){
            peripheralCap.invalidate();
            peripheralCap = null;
        }
    }

    @Override
    public void tickServer() {
        super.tickServer();
        syncForNear(true, FIELD);
        syncAttachedPropeller();
        syncAttachedInducer();
    }


    @Override
    public void lazyTick() {
        super.lazyTick();
        if(level == null || level.isClientSide)return;
        ExposedFieldSyncClientPacket.syncClient(this, getBlockPos(), level);
    }

    public PropellerControllerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        /*
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(
                        rotationalSpeed::read,
                        rotationalSpeed::write,
                        SerializeUtils.DOUBLE,
                        SharedKeys.VALUE
                ),
                Side.SHARED
        );

        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> ITerminalDevice.super.deserialize(tag.getCompound("fields")),
                        tag -> tag.put("fields", ITerminalDevice.super.serialize()),
                        FIELD),
                Side.SHARED
        );
        * */

        buildRegistry(SharedKeys.VALUE)
            .withBasic(SerializePort.of(rotationalSpeed::read, rotationalSpeed::write, SerializeUtils.DOUBLE))
            .withClient(ClientBuffer.DOUBLE.get())
            .register();

        buildRegistry(FIELD)
                .withBasic(CompoundTagPort.of(
                        ITerminalDevice.super::serialize,
                        ITerminalDevice.super::deserializeUnchecked
                ))
                .withClient(
                        new ClientBuffer<>(SerializeUtils.UNIT, CompoundTag.class)
                )
                .dispatchToSync()
                .register();

    }

    public void syncAttachedPropeller(){
        if(level == null)return;
        Vec3i direction = this.getBlockState().getValue(BlockStateProperties.FACING).getNormal();
        BlockPos propellerPos = this.getBlockPos().offset(new BlockPos(direction.getX(), direction.getY(), direction.getZ()));
        var attachedBlockEntity = level.getExistingBlockEntity(propellerPos);
        hasAttachedPropeller = attachedBlockEntity instanceof PropellerBlockEntity;
        if(!hasAttachedPropeller)return;
        PropellerBlockEntity propeller = (PropellerBlockEntity) attachedBlockEntity;
        propeller.setVisualRotationalSpeed(rotationalSpeed.read());
        attachedPropellerTorqueRatio = propeller.getTorqueRatio();
        attachedPropellerThrustRatio = propeller.getThrustRatio();
    }

    public boolean canDrive(){
        return hasAttachedPropeller;
    }


    public double getTargetSpeed(){
        return rotationalSpeed.read();
    }

    public void syncAttachedInducer(){
        if(level != null && level.isClientSide)return;
        Optional
                .ofNullable(getLoadedServerShip())
                .map(PropellerForceInducer::getOrCreate)
                .ifPresent(inducer -> inducer.replace(
                        WorldBlockPos.of(level, getBlockPos()),
                        this::getLogicalPropeller
                ));
    }

    /*
    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        rotationalSpeed.write((double)speed);
    }
    * */


    @Override
    public void remove() {
        super.remove();
        setTargetSpeed(0);
        syncAttachedPropeller();
    }

    public void setTargetSpeed(double speed){
        rotationalSpeed.write(speed);
    }

    public @Nullable LogicalPropeller getLogicalPropeller() {
        if(!isOnShip())return null;
        return new LogicalPropeller(
                canDrive(),
                getDirectionJOML(),
                getTargetSpeed(),
                attachedPropellerThrustRatio,
                attachedPropellerTorqueRatio,
                WorldBlockPos.of(level, getBlockPos())
        );
    }

    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return ITerminalDevice.super.TerminalDeviceToolTip(tooltip, isPlayerSneaking);
    }

    @Override
    public String name() {
        return "propeller controller";
    }

    public void displayScreen(ServerPlayer player){
        var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.OPEN_SCREEN_0)
                .withDouble(rotationalSpeed.read())
                .build();

        ControlCraftPackets.sendToPlayer(p, player);
    }



    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if (packet.getType() == RegisteredPacketType.SETTING_0) {
            double speed = packet.getDoubles().get(0);
            setTargetSpeed(speed);
        }
    }
}
