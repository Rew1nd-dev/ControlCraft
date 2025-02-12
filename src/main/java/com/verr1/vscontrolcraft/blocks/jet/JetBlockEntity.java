package com.verr1.vscontrolcraft.blocks.jet;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.base.DataStructure.SynchronizedField;
import com.verr1.vscontrolcraft.base.OnShipDirectinonalBlockEntity;
import com.verr1.vscontrolcraft.base.UltraTerminal.*;
import com.verr1.vscontrolcraft.blocks.jetRudder.JetRudderBlockEntity;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.JetPeripheral;
import com.verr1.vscontrolcraft.compat.valkyrienskies.jet.JetForceInducer;
import com.verr1.vscontrolcraft.compat.valkyrienskies.jet.LogicalJet;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.List;

public class JetBlockEntity extends OnShipDirectinonalBlockEntity implements
        ITerminalDevice, IPacketHandler
{

    public SynchronizedField<Double> horizontalAngle = new SynchronizedField<>(0.0);
    public SynchronizedField<Double> verticalAngle = new SynchronizedField<>(0.0);
    public SynchronizedField<Double> thrust = new SynchronizedField<>(0.0);

    public SynchronizedField<ShipPhysics> physics = new SynchronizedField<>(ShipPhysics.EMPTY);

    public boolean canVectorize = false;

    private JetPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    thrust::read,
                    thrust::write,
                    "thrust",
                    WidgetType.SLIDE,
                    ExposedFieldType.THRUST
            ),
            new ExposedFieldWrapper(
                    horizontalAngle::read,
                    horizontalAngle::write,
                    "horizontal",
                    WidgetType.SLIDE,
                    ExposedFieldType.HORIZONTAL_TILT
            ),
            new ExposedFieldWrapper(
                    verticalAngle::read,
                    verticalAngle::write,
                    "vertical",
                    WidgetType.SLIDE,
                    ExposedFieldType.VERTICAL_TILT
            )
    );

    private ExposedFieldWrapper exposedField = fields.get(0);

    @Override
    public ExposedFieldWrapper getExposedField() {
        return exposedField;
    }

    @Override
    public void setExposedField(ExposedFieldType type, double min, double max) {
        switch (type){
            case THRUST -> exposedField = fields.get(0);
            case HORIZONTAL_TILT -> exposedField = fields.get(1);
            case VERTICAL_TILT -> exposedField = fields.get(2);
        }

        exposedField.min_max = new Vector2d(min, max);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new JetPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }



    public Direction getVertical(){
        Direction currentDir = getDirection();
        if(currentDir.getAxis() != Direction.Axis.Y){
            return Direction.UP;
        }
        if(currentDir == Direction.UP)return Direction.NORTH;
        return Direction.SOUTH;
    }

    public Direction getHorizontal(){
        return getVertical().getCounterClockWise(getDirection().getAxis());
    }

    public Vector3d getVerticalJOML(){
        return Util.Vec3itoVector3d(getVertical().getNormal());
    }

    public Vector3d getHorizontalJOML(){
        return Util.Vec3itoVector3d(getHorizontal().getNormal());
    }

    public static Vector3d getThrustDir(double h, double v, Vector3dc basis_h, Vector3dc basis_v, Vector3dc basis_t){
        double sh = Math.sin(h);
        double sv = Math.sin(v);
        double st = Math.sqrt(Math.abs(1 - 0.5 * (sh * sh + sv * sv))); // in case of < 0

        Vector3d dir =
                new Vector3d(
                ).fma(
                        sh,
                        basis_h
                ).fma(
                        sv,
                        basis_v
                ).fma(
                        st,
                        basis_t
                ).normalize();
        return dir;
    }



    public LogicalJet getLogicalJet(){
        Vector3dc basis_h = getHorizontalJOML();
        Vector3dc basis_v = getVerticalJOML();
        Vector3dc basis_t = getDirectionJOML();

        double h = canVectorize ? horizontalAngle.read() : 0;
        double v = canVectorize ? verticalAngle.read() : 0;

        Vector3d dir = getThrustDir(h, v,basis_h, basis_v, basis_t);

        double t = thrust.read();

        return new LogicalJet(dir, t);
    }

    public void syncAttachedJet(){
        if(level.isClientSide)return;
        BlockPos jetPos = getBlockPos().relative(getDirection().getOpposite());
        if(!(level.getExistingBlockEntity(jetPos) instanceof JetRudderBlockEntity jet)){
            canVectorize = false;
            return;
        };
        canVectorize = true;
        jet.setAnimatedAngles(
                horizontalAngle.read(),
                verticalAngle.read(),
                thrust.read()
        );
    }


    public void syncAttachedInducer(){
        if(level.isClientSide) return;
        ServerShip ship = getServerShipOn();
        if(ship == null)return;
        var inducer = JetForceInducer.getOrCreate(ship);
        inducer.update(new LevelPos(getBlockPos(), (ServerLevel) level));
    }

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide)return;
        syncAttachedJet();
        syncAttachedInducer();
    }



    public JetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    public String name() {
        return "Jet";
    }

    public void displayScreen(ServerPlayer player){
        double h = horizontalAngle.read();
        double v = verticalAngle.read();
        double t = thrust.read();
        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.OPEN_SCREEN)
                .withDouble(h)
                .withDouble(v)
                .withDouble(t)
                .build();

        AllPackets.sendToPlayer(p, player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == BlockBoundPacketType.OPEN_SCREEN){
            double h = packet.getDoubles().get(0);
            double v = packet.getDoubles().get(1);
            double t = packet.getDoubles().get(2);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ScreenOpener.open(new JetSettingsScreen(t, h, v, packet.getBoundPos())
            ));
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SETTING){
            double t = packet.getDoubles().get(0);
            double h = packet.getDoubles().get(1);
            double v = packet.getDoubles().get(2);
            thrust.write(t);
            horizontalAngle.write(h);
            verticalAngle.write(v);
        }
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if(clientPacket)return;
        fields.forEach(e -> tag.put("field_" + e.type.name(), e.serialize()));
        tag.putInt("exposedField", fields.indexOf(exposedField));

    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if(clientPacket)return;

        fields.forEach(f -> f.deserialize(tag.getCompound("field_" + f.type.name())));
        try{
            exposedField = fields.get(tag.getInt("exposed_field"));
        }catch (IndexOutOfBoundsException e){
            exposedField = fields.get(0);
        }
    }

}
