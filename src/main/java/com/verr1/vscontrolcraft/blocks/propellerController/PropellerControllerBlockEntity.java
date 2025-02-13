package com.verr1.vscontrolcraft.blocks.propellerController;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.base.DataStructure.SynchronizedField;
import com.verr1.vscontrolcraft.base.OnShipDirectinonalBlockEntity;
import com.verr1.vscontrolcraft.base.UltraTerminal.*;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.PropellerControllerPeripheral;
import com.verr1.vscontrolcraft.compat.valkyrienskies.propeller.LogicalPropeller;
import com.verr1.vscontrolcraft.compat.valkyrienskies.propeller.PropellerForceInducer;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerBlockEntity;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class PropellerControllerBlockEntity extends OnShipDirectinonalBlockEntity implements
        ITerminalDevice, IPacketHandler
{
    public boolean hasAttachedPropeller = false;

    public SynchronizedField<Double> rotationalSpeed = new SynchronizedField<>(0.0);

    public boolean attachPropellerReverseTorque = false;
    public double attachedPropellerThrustRatio = 0;
    public double attachedPropellerTorqueRatio = 0;


    private PropellerControllerPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    rotationalSpeed::read,
                    rotationalSpeed::write,
                    "Speed",
                    WidgetType.SLIDE,
                    ExposedFieldType.SPEED
            )
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
    public void tick(){
        super.tick();
        if(level.isClientSide) return;
        syncAttachedPropeller();
        syncAttachedInducer();
    }

    public PropellerControllerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public void syncAttachedPropeller(){
        Vec3i direction = this.getBlockState().getValue(BlockStateProperties.FACING).getNormal();
        BlockPos propellerPos = this.getBlockPos().offset(new BlockPos(direction.getX(), direction.getY(), direction.getZ()));
        var attachedBlockEntity = level.getExistingBlockEntity(propellerPos);
        hasAttachedPropeller = attachedBlockEntity instanceof PropellerBlockEntity;
        if(!hasAttachedPropeller)return;
        PropellerBlockEntity propeller = (PropellerBlockEntity) attachedBlockEntity;
        propeller.setVisualRotationalSpeed(rotationalSpeed.read());
        attachedPropellerTorqueRatio = propeller.getTorqueRatio();
        attachedPropellerThrustRatio = propeller.getThrustRatio();
        attachPropellerReverseTorque = propeller.getReverseTorque();
    }

    public boolean canDrive(){
        return hasAttachedPropeller;
    }


    public double getTargetSpeed(){
        return rotationalSpeed.read();
    }

    public void syncAttachedInducer(){
        if(level.isClientSide) return;
        ServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) level, getBlockPos());

        if(ship == null)return;
        var inducer = PropellerForceInducer.getOrCreate(ship);
        inducer.update(new LevelPos(getBlockPos(), (ServerLevel) level));
    }


    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        rotationalSpeed.write((double)speed);
    }

    @Override
    public void destroy(){
        super.destroy();
    }

    public void setTargetSpeed(double speed){
        rotationalSpeed.write(speed);
    }

    public LogicalPropeller getLogicalPropeller() {
        if(level.isClientSide)return null;
        if(!isOnServerShip())return null;
        return  new LogicalPropeller(
                canDrive(),
                attachPropellerReverseTorque,
                getDirectionJOML(),
                getTargetSpeed(),
                attachedPropellerThrustRatio,
                attachedPropellerTorqueRatio,
                (ServerLevel) this.level
        );
    }

    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    private ExposedFieldWrapper exposedField = fields.get(0);

    @Override
    public ExposedFieldWrapper getExposedField() {
        return exposedField;
    }

    @Override
    public void setExposedField(ExposedFieldType type, double min, double max, ExposedFieldDirection openTo) {
        if (type == ExposedFieldType.SPEED) {
            exposedField = fields.get(0);
        }
        exposedField.min_max = new Vector2d(min, max);
    }

    @Override
    public String name() {
        return "propeller controller";
    }

    public void displayScreen(ServerPlayer player){
        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.OPEN_SCREEN)
                .withDouble(rotationalSpeed.read())
                .build();

        AllPackets.sendToPlayer(p, player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == BlockBoundPacketType.OPEN_SCREEN){
            double speed = packet.getDoubles().get(0);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ScreenOpener.open(new PropellerControllerScreen(getBlockPos(), speed));
            });
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if (packet.getType() == BlockBoundPacketType.SETTING) {
            double speed = packet.getDoubles().get(0);
            setTargetSpeed(speed);
        }
    }
}
