package com.verr1.controlcraft.content.blocks.propeller;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.content.blocks.SharedKeys;
import com.verr1.controlcraft.content.valkyrienskies.attachments.PropellerForceInducer;
import com.verr1.controlcraft.foundation.data.NumericField;
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
import com.verr1.controlcraft.foundation.redstone.DirectReceiver;
import com.verr1.controlcraft.foundation.redstone.IReceiver;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
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
        IPacketHandler, IHaveGoggleInformation, IReceiver
{
    public boolean hasAttachedPropeller = false;

    public SynchronizedField<Double> rotationalSpeed = new SynchronizedField<>(0.0);

    public double attachedPropellerThrustRatio = 0;
    public double attachedPropellerTorqueRatio = 0;


    private PropellerControllerPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;



    private final DirectReceiver receiver = new DirectReceiver();


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
        // syncForNear(true, FIELD);
        syncAttachedPropeller();
        syncAttachedInducer();
    }


    @Override
    public void lazyTick() {
        super.lazyTick();
        if(level == null || level.isClientSide)return;
        // ExposedFieldSyncClientPacket.syncClient(this, getBlockPos(), level);
    }

    public PropellerControllerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);

        buildRegistry(SharedKeys.VALUE)
            .withBasic(SerializePort.of(rotationalSpeed::read, rotationalSpeed::write, SerializeUtils.DOUBLE))
            .withClient(ClientBuffer.DOUBLE.get())
            .register();

        /*

        * */

        buildRegistry(FIELD_)
                .withBasic(CompoundTagPort.of(
                        () -> receiver().serialize(),
                        t -> receiver().deserialize(t)
                ))
                .withClient(
                        new ClientBuffer<>(SerializeUtils.UNIT, CompoundTag.class)
                )
                .dispatchToSync()
                .register();

        receiver.register(
                new NumericField(
                        this::getTargetSpeed,
                        this::setTargetSpeed,
                        "Speed"
                ),
                new DirectReceiver.InitContext(SlotType.SPEED  , Couple.create(0.0,  64.0)),
                8
        );


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
    @OnlyIn(Dist.CLIENT)
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return receiver().makeToolTip(tooltip, isPlayerSneaking);
    }

    @Override
    public DirectReceiver receiver() {
        return receiver;
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
