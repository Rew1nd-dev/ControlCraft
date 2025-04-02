package com.verr1.controlcraft.content.blocks.jet;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.type.Side;
import com.verr1.controlcraft.content.cctweaked.peripheral.JetPeripheral;
import com.verr1.controlcraft.content.gui.legacy.JetScreen;
import com.verr1.controlcraft.content.valkyrienskies.attachments.JetForceInducer;
import com.verr1.controlcraft.foundation.api.IPacketHandler;
import com.verr1.controlcraft.foundation.api.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.SynchronizedField;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.data.logical.LogicalJet;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.network.packets.specific.ExposedFieldSyncClientPacket;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.SerializeUtils;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.Optional;

public class JetBlockEntity extends OnShipBlockEntity implements
        ITerminalDevice, IPacketHandler, IHaveGoggleInformation
{

    public SynchronizedField<Double> horizontalAngle = new SynchronizedField<>(0.0);
    public SynchronizedField<Double> verticalAngle = new SynchronizedField<>(0.0);
    public SynchronizedField<Double> thrust = new SynchronizedField<>(0.0);

    public static NetworkKey THRUST = NetworkKey.create("thrust");
    public static NetworkKey HORIZONTAL_ANGLE = NetworkKey.create("horizontal_angle");
    public static NetworkKey VERTICAL_ANGLE = NetworkKey.create("vertical_angle");


    public boolean canVectorize = false;

    private JetPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    thrust::read,
                    thrust::write,
                    "thrust",
                    ExposedFieldType.THRUST
            ).withSuggestedRange(0, 10000),
            new ExposedFieldWrapper(
                    horizontalAngle::read,
                    horizontalAngle::write,
                    "horizontal",
                    ExposedFieldType.HORIZONTAL_TILT
            ).withSuggestedRange(-Math.PI / 2, Math.PI / 2),
            new ExposedFieldWrapper(
                    verticalAngle::read,
                    verticalAngle::write,
                    "vertical",
                    ExposedFieldType.VERTICAL_TILT
            ).withSuggestedRange(-Math.PI / 2, Math.PI / 2),
            new ExposedFieldWrapper(
                    horizontalAngle::read,
                    horizontalAngle::write,
                    "horizontal",
                    ExposedFieldType.HORIZONTAL_TILT$1
            ).withSuggestedRange(-Math.PI / 2, Math.PI / 2),
            new ExposedFieldWrapper(
                    verticalAngle::read,
                    verticalAngle::write,
                    "vertical",
                    ExposedFieldType.VERTICAL_TILT$1
            ).withSuggestedRange(-Math.PI / 2, Math.PI / 2)
    );

    private ExposedFieldWrapper exposedField = fields.get(0);



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
        return ValkyrienSkies.set(new Vector3d(), getVertical().getNormal());
    }

    public Vector3d getHorizontalJOML(){
        return ValkyrienSkies.set(new Vector3d(), getHorizontal().getNormal());
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



    public @Nullable LogicalJet getLogicalJet(){
        if(level == null)return null;
        Vector3dc basis_h = getHorizontalJOML();
        Vector3dc basis_v = getVerticalJOML();
        Vector3dc basis_t = getDirectionJOML();

        double h = canVectorize ? horizontalAngle.read() : 0;
        double v = canVectorize ? verticalAngle.read() : 0;

        Vector3d dir = getThrustDir(h, v, basis_h, basis_v, basis_t);

        double t = thrust.read();

        return new LogicalJet(dir, t, WorldBlockPos.of(level, getBlockPos()));
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
        if(level == null || level.isClientSide) return;
        Optional
            .ofNullable(getLoadedServerShip())
            .map(JetForceInducer::getOrCreate)
            .ifPresent(inducer -> inducer.alive(WorldBlockPos.of(level, getBlockPos())));
    }

    @Override
    public void tickServer() {
        syncAttachedJet();
        syncAttachedInducer();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if(level.isClientSide)return;
        ExposedFieldSyncClientPacket.syncClient(this, getBlockPos(), level);
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
        var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.OPEN_SCREEN_0)
                .withDouble(h)
                .withDouble(v)
                .withDouble(t)
                .build();

        ControlCraftPackets.sendToPlayer(p, player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == RegisteredPacketType.OPEN_SCREEN_0){
            double h = packet.getDoubles().get(0);
            double v = packet.getDoubles().get(1);
            double t = packet.getDoubles().get(2);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ScreenOpener.open(new JetScreen(t, h, v, packet.getBoundPos())
            ));
        }
    }


    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == RegisteredPacketType.SETTING_0){
            double t = packet.getDoubles().get(0);
            double h = packet.getDoubles().get(1);
            double v = packet.getDoubles().get(2);
            thrust.write(t);
            horizontalAngle.write(h);
            verticalAngle.write(v);
        }
    }

    public JetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        registerFieldReadWriter(SerializeUtils.ReadWriter.of(
                thrust::read,
                thrust::write,
                SerializeUtils.DOUBLE,
                THRUST
        ),
                Side.SHARED
        );

        registerFieldReadWriter(SerializeUtils.ReadWriter.of(
                horizontalAngle::read,
                horizontalAngle::write,
                SerializeUtils.DOUBLE,
                HORIZONTAL_ANGLE
        ),
                Side.SHARED
        );

        registerFieldReadWriter(SerializeUtils.ReadWriter.of(
                verticalAngle::read,
                verticalAngle::write,
                SerializeUtils.DOUBLE,
                VERTICAL_ANGLE
        ),
                Side.SHARED
        );

        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> ITerminalDevice.super.deserialize(tag.getCompound("fields")),
                        tag -> tag.put("fields", ITerminalDevice.super.serialize()),
                        FIELD),
                Side.SHARED
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return ITerminalDevice.super.TerminalDeviceToolTip(tooltip, isPlayerSneaking);
    }
}
