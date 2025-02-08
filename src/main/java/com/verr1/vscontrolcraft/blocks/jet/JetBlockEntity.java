package com.verr1.vscontrolcraft.blocks.jet;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.base.DataStructure.SynchronizedField;
import com.verr1.vscontrolcraft.base.OnShipDirectinonalBlockEntity;
import com.verr1.vscontrolcraft.base.UltraTerminal.ITerminalDevice;
import com.verr1.vscontrolcraft.base.UltraTerminal.NumericField;
import com.verr1.vscontrolcraft.base.UltraTerminal.WidgetType;
import com.verr1.vscontrolcraft.blocks.jetRudder.JetRudderBlockEntity;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.JetPeripheral;
import com.verr1.vscontrolcraft.compat.valkyrienskies.jet.JetForceInducer;
import com.verr1.vscontrolcraft.compat.valkyrienskies.jet.LogicalJet;
import com.verr1.vscontrolcraft.utils.Util;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.List;

public class JetBlockEntity extends OnShipDirectinonalBlockEntity implements
        ITerminalDevice
{

    public SynchronizedField<Double> horizontalAngle = new SynchronizedField<>(0.0);
    public SynchronizedField<Double> verticalAngle = new SynchronizedField<>(0.0);
    public SynchronizedField<Double> thrust = new SynchronizedField<>(0.0);

    public SynchronizedField<ShipPhysics> physics = new SynchronizedField<>(ShipPhysics.EMPTY);

    public boolean canVectorize = false;

    private JetPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    private final List<NumericField> fields = List.of(
            new NumericField(
                    thrust::read,
                    thrust::write,
                    "thrust",
                    WidgetType.SLIDE
            ),
            new NumericField(
                    horizontalAngle::read,
                    horizontalAngle::write,
                    "horizontal",
                    WidgetType.SLIDE
            ),
            new NumericField(
                    verticalAngle::read,
                    verticalAngle::write,
                    "vertical",
                    WidgetType.SLIDE
            )
    );

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
    public List<NumericField> fields() {
        return fields;
    }

    @Override
    public String name() {
        return "Jet";
    }
}
