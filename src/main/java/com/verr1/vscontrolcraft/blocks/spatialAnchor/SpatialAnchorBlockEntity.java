package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.base.OnShipDirectinonalBlockEntity;
import com.verr1.vscontrolcraft.base.Servo.ICanBruteDirectionalConnect;
import com.verr1.vscontrolcraft.base.UltraTerminal.ITerminalDevice;
import com.verr1.vscontrolcraft.base.UltraTerminal.NumericField;
import com.verr1.vscontrolcraft.base.UltraTerminal.WidgetType;
import com.verr1.vscontrolcraft.blocks.jointMotor.JointMotorBlock;
import com.verr1.vscontrolcraft.compat.valkyrienskies.spatial.LogicalSpatial;
import com.verr1.vscontrolcraft.compat.valkyrienskies.spatial.SpatialForceInducer;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.List;

public class SpatialAnchorBlockEntity extends OnShipDirectinonalBlockEntity implements
        ICanBruteDirectionalConnect , ITerminalDevice
{

    private boolean isRunning = false;
    private ISpatialTarget tracking = null;
    private boolean isStatic = false;


    private double anchorOffset = 1.0;
    private long protocol = 0;
    private final int MAX_DISTANCE_SQRT_CAN_LINK = 32 * 32;
    private final SpatialScheduleInfoHolder schedule = new SpatialScheduleInfoHolder().withPID(18, 3, 12, 10);

    private final List<NumericField> fields = List.of(
            new NumericField(
                    this::getAnchorOffset,
                    this::setAnchorOffset,
                    "offset",
                    WidgetType.SLIDE
            ),
            new NumericField(
                    () -> (double) (isRunning() ? 1 : 0),
                    v -> setRunning(v > 7.5) ,
                    "running",
                    WidgetType.TOGGLE
            ),
            new NumericField(
                    () -> (double)(isStatic() ? 0 : 1),
                    v -> setStatic(v > 7.5),
                    "dynamic",
                    WidgetType.TOGGLE
            ),
            new NumericField(
                    () -> this.getSchedule().getPp(),
                    v -> this.getSchedule().setPp(v),
                    "P",
                    WidgetType.SLIDE
            ),
            new NumericField(
                    () -> this.getSchedule().getI(),
                    v -> this.getSchedule().setI(v),
                    "I",
                    WidgetType.SLIDE
            ),
            new NumericField(
                    () -> this.getSchedule().getDp(),
                    v -> this.getSchedule().setDp(v),
                    "D",
                    WidgetType.SLIDE
            )
    );

    public SpatialScheduleInfoHolder getSchedule() {
        return schedule;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public ISpatialTarget getTracking() {
        return tracking;
    }

    public LogicalSpatial getLogicalSpatial(){
        if(level.isClientSide)return null;
        return new LogicalSpatial(
                (ServerLevel) level,
                getBlockPos(),
                getAlign(),
                getForward(),
                getServerShipID(),
                getDimensionID(),
                shouldDrive(),
                isStatic,
                protocol
        );
    }

    public void setProtocol(long protocol) {
        this.protocol = protocol;
    }

    public long getProtocol() {
        return protocol;
    }

    public boolean shouldDrive(){
        return isRunning && !isStatic && tracking != null;
    }

    public SpatialAnchorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setAnchorOffset(double anchorOffset) {
        this.anchorOffset = anchorOffset;
    }

    public double getAnchorOffset() {
        return anchorOffset;
    }

    public void setStatic(Boolean aStatic) {
        isStatic = aStatic;
    }

    public boolean isStatic() {
        return isStatic;
    }


    public void setRunning(boolean running) {
        isRunning = running;
    }

    public Direction getVertical(){
        boolean isFlipped = getBlockState().getValue(SpatialAnchorBlock.FLIPPED);
        return isFlipped ? getVerticalUnflipped().getOpposite() : getVerticalUnflipped();
    }

    public Direction getVerticalUnflipped(){
        Direction facing = getBlockState().getValue(JointMotorBlock.FACING);
        Boolean align = getBlockState().getValue(JointMotorBlock.AXIS_ALONG_FIRST_COORDINATE);
        if(facing.getAxis() != Direction.Axis.X){
            if(align)return Direction.EAST;
            return facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        }
        if(align)return Direction.UP;
        return Direction.SOUTH;
    }

    public boolean poweredToRun(){
        if(level.isClientSide)return false;
        return level.getSignal(getBlockPos().relative(getAlign().getOpposite()), getAlign()) != 0;
    }

    public boolean poweredToDynamize(){
        if(level.isClientSide)return false;
        return level.getSignal(getBlockPos().relative(getForward().getOpposite()), getForward()) != 0;
    }

    public void activate(){
        if(level.isClientSide)return;
        SpatialLinkManager.activate(getLogicalSpatial());
    }

    public void trackNearest(){
        ISpatialTarget nearest = SpatialLinkManager.link(getLogicalSpatial());
        setTracking(nearest);
    }

    public void setTracking(ISpatialTarget tracking) {
        if(!filter(tracking)){
            this.tracking = null;
            return;
        }
        this.tracking = tracking;
    }

    public void updateSchedule(){
        if(level.isClientSide)return;
        if(!isOnServerShip())return;
        if(getTracking() == null || getTracking().pos() == getBlockPos() || isStatic)return;


        updateSchedule(tracking);
    }

    public boolean filter(ISpatialTarget tracking){
        if(tracking == null)return false;
        if(tracking.pos() == getBlockPos())return false;
        if(!getDimensionID().equals(tracking.dimensionID()))return false;
        if(tracking.shipID() == getServerShipID())return false;
        return true;
    }

    private void updateSchedule(ISpatialTarget spatial){
        if(level.isClientSide)return;
        ServerShip ship = getServerShipOn();
        if(ship == null)return;

        BlockPos c_pos = getBlockPos();
        Direction c_align = getDirection();
        Direction c_forward = getVertical();

        Quaterniondc q_base = spatial.qBase();

        Quaterniondc q_extra = VSMathUtils.rotationToAlign(
                spatial.align(),
                spatial.forward(),
                c_align,
                c_forward
        );

        Quaterniondc q_target = q_base.mul(q_extra, new Quaterniond());

        Vector3dc dir = Util.Vec3itoVector3d(c_align.getNormal()).mul(anchorOffset);
        Vector3dc cFace_sc = Util.Vec3itoVector3d(c_pos).add(dir);
        Vector3dc cCenter_sc = ship.getInertiaData().getCenterOfMassInShip();
        Vector3dc relative_r_sc = new Vector3d(cFace_sc).sub(cCenter_sc, new Vector3d());

        Vector3dc relative_r_wc = q_target.transform(relative_r_sc, new Vector3d());
        Vector3dc tFace_wc = spatial.vPos();

        Vector3dc p_target = new Vector3d(tFace_wc).sub(relative_r_wc, new Vector3d());

        schedule.overrideTarget(q_target, p_target);
    }

    public void syncAttachedInducer(){
        if(level.isClientSide)return;
        ServerShip ship = getServerShipOn();
        if(ship == null)return;
        var inducer = SpatialForceInducer.getOrCreate(ship);
        inducer.update(new LevelPos(getBlockPos(), (ServerLevel)level));
    }

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide)return;

        if(isRunning()){
            activate();
            trackNearest();
        }

        syncAttachedInducer();
        updateSchedule();
    }

    @Override
    public void bruteDirectionalConnectWith(BlockPos pos, Direction align, Direction forward) {
        if(level.isClientSide)return;
        // just make a dummy
        tracking = new LogicalSpatial(
                (ServerLevel)level,
                pos,
                align,
                forward,
                VSMathUtils.getServerShipID(pos, (ServerLevel)level ),
                getDimensionID(),
                true,
                true,
                protocol
        );
    }

    @Override
    public Direction getAlign() {
        return getDirection();
    }

    @Override
    public Direction getForward() {
        return getVertical();
    }


    @Override
    public List<NumericField> fields() {
        return fields;
    }

    @Override
    public String name() {
        return "spatial anchor";
    }
}
