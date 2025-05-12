package com.verr1.controlcraft.content.blocks.jet;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.content.valkyrienskies.attachments.JetForceInducer;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.data.NumericField;
import com.verr1.controlcraft.foundation.network.executors.ClientBuffer;
import com.verr1.controlcraft.foundation.network.executors.CompoundTagPort;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.content.cctweaked.peripheral.JetPeripheral;
import com.verr1.controlcraft.foundation.api.IPacketHandler;
import com.verr1.controlcraft.foundation.api.delegate.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.SynchronizedField;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.data.logical.LogicalJet;
import com.verr1.controlcraft.foundation.redstone.DirectReceiver;
import com.verr1.controlcraft.foundation.redstone.IReceiver;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.utils.MathUtils;
import com.verr1.controlcraft.utils.SerializeUtils;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.Optional;

public class JetBlockEntity extends OnShipBlockEntity implements
        IReceiver, IPacketHandler, IHaveGoggleInformation
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

    private final DirectReceiver receiver = new DirectReceiver();

    private static final double SQRT_2 = Math.sqrt(2);

    @Override
    public DirectReceiver receiver() {
        return receiver;
    }

    public void setOutputThrust(Vector3d hvt){

        Vector3d hvt_norm = MathUtils.safeNormalize(hvt);
        double scale = hvt.length();

        double h = Math.asin(hvt_norm.x);
        double cosh = Math.cos(h);
        cosh = Math.signum(cosh) * MathUtils.eps + cosh;
        double v = Math.asin(MathUtils.clamp(hvt_norm.y / cosh, 1 - MathUtils.eps));


        horizontalAngle.write(h);
        verticalAngle.write(v);
        thrust.write(scale);
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
        return ValkyrienSkies.set(new Vector3d(), getVertical().getNormal());
    }

    public Vector3d getHorizontalJOML(){
        return ValkyrienSkies.set(new Vector3d(), getHorizontal().getNormal());
    }

    public static Vector3d getThrustDir(double h, double v, Vector3dc basis_h, Vector3dc basis_v, Vector3dc basis_t){
        double sh = Math.sin(h);
        double sv = Math.sin(v) * Math.cos(h);
        double st = Math.cos(v) * Math.cos(h);// Math.sqrt(Math.abs(0.5 - 0.5 * (sh * sh + sv * sv))); // in case of < 0

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
        if(level == null || level.isClientSide)return;
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
            .ifPresent(inducer -> inducer.replace(
                    WorldBlockPos.of(level, getBlockPos()),
                    this::getLogicalJet
            ));
    }

    @Override
    public void tickServer() {
        syncAttachedJet();
        syncAttachedInducer();
        syncForNear(true, FIELD_);
    }



    @Override
    public String name() {
        return "Jet";
    }







    public JetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

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

        buildRegistry(THRUST)
                .withBasic(SerializePort.of(
                        thrust::read,
                        thrust::write,
                        SerializeUtils.DOUBLE
                ))
                .withClient(new ClientBuffer<>(SerializeUtils.DOUBLE, Double.class))
                .dispatchToSync()
                .register();

        buildRegistry(HORIZONTAL_ANGLE)
                .withBasic(SerializePort.of(
                        horizontalAngle::read,
                        horizontalAngle::write,
                        SerializeUtils.DOUBLE
                ))
                .withClient(new ClientBuffer<>(SerializeUtils.DOUBLE, Double.class))
                .dispatchToSync()
                .register();

        buildRegistry(VERTICAL_ANGLE)
                .withBasic(SerializePort.of(
                        verticalAngle::read,
                        verticalAngle::write,
                        SerializeUtils.DOUBLE
                ))
                .withClient(new ClientBuffer<>(SerializeUtils.DOUBLE, Double.class))
                .dispatchToSync()
                .register();

        receiver()
            .register(
                new NumericField(
                        horizontalAngle::read,
                        horizontalAngle::write,
                        "horizontal"
                ),
                new DirectReceiver.InitContext(SlotType.HORIZONTAL_TILT, Couple.create(0.0, 10000.0)),
                6
        )
            .register(
                new NumericField(
                        verticalAngle::read,
                        verticalAngle::write,
                        "vertical"
                ),
                new DirectReceiver.InitContext(SlotType.VERTICAL_TILT, Couple.create(0.0, 10000.0)),
                6
        )
            .register(
                new NumericField(
                        thrust::read,
                        thrust::write,
                        "horizontal"
                ),
                new DirectReceiver.InitContext(SlotType.THRUST, Couple.create(0.0, 10000.0)),
                4
        );

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return receiver().makeToolTip(tooltip, isPlayerSneaking)
        ;
    }
}
