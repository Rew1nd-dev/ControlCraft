package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.base.OnShipDirectinonalBlockEntity;
import com.verr1.vscontrolcraft.base.Servo.ICanBruteDirectionalConnect;
import com.verr1.vscontrolcraft.base.UltraTerminal.*;
import com.verr1.vscontrolcraft.blocks.jointMotor.JointMotorBlock;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.PropellerControllerPeripheral;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.SpatialAnchorPeripheral;
import com.verr1.vscontrolcraft.compat.valkyrienskies.spatial.LogicalSpatial;
import com.verr1.vscontrolcraft.compat.valkyrienskies.spatial.SpatialForceInducer;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
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
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.List;

public class SpatialAnchorBlockEntity extends OnShipDirectinonalBlockEntity implements
        ICanBruteDirectionalConnect , ITerminalDevice, IPacketHandler
{

    private boolean isRunning = false;
    private ISpatialTarget tracking = null;
    private boolean isStatic = false;


    private double anchorOffset = 1.0;
    private long protocol = 0;
    private final int MAX_DISTANCE_SQRT_CAN_LINK = 32 * 32;
    private final SpatialScheduleInfoHolder schedule = new SpatialScheduleInfoHolder().withPPID(18, 3, 12, 10);

    private SpatialAnchorPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    this::getAnchorOffset,
                    this::setAnchorOffset,
                    "offset",
                    WidgetType.SLIDE,
                    ExposedFieldType.OFFSET
            ),
            new ExposedFieldWrapper(
                    () -> (double) (isRunning() ? 1 : 0),
                    v -> setRunning(v > 0.5) ,
                    "running",
                    WidgetType.TOGGLE,
                    ExposedFieldType.IS_RUNNING
            ),
            new ExposedFieldWrapper(
                    () -> (double)(isStatic() ? 0 : 1),
                    v -> setStatic(v > 0.5),
                    "dynamic",
                    WidgetType.TOGGLE,
                    ExposedFieldType.IS_STATIC
            ),
            new ExposedFieldWrapper(
                    () -> this.getSchedule().getPp(),
                    v -> this.getSchedule().setPp(v),
                    "P",
                    WidgetType.SLIDE,
                    ExposedFieldType.P
            ),
            new ExposedFieldWrapper(
                    () -> this.getSchedule().getIp(),
                    v -> this.getSchedule().setIp(v),
                    "I",
                    WidgetType.SLIDE,
                    ExposedFieldType.I
            ),
            new ExposedFieldWrapper(
                    () -> this.getSchedule().getDp(),
                    v -> this.getSchedule().setDp(v),
                    "D",
                    WidgetType.SLIDE,
                    ExposedFieldType.D
            )
    );

    private ExposedFieldWrapper exposedField = fields.get(0);

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

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new SpatialAnchorPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
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
        if(tracking
                .vPos()
                .sub(VSMathUtils.getAbsolutePosition(this), new Vector3d())
                .lengthSquared() > MAX_DISTANCE_SQRT_CAN_LINK)return false;

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
        syncClient();
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
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    public ExposedFieldWrapper getExposedField() {
        return exposedField;
    }

    @Override
    public void setExposedField(ExposedFieldType type, double min, double max) {
        switch (type){
            case OFFSET -> exposedField = fields.get(0);
            case IS_RUNNING -> exposedField = fields.get(1);
            case IS_STATIC -> exposedField = fields.get(2);
            case P -> exposedField = fields.get(3);
            case I -> exposedField = fields.get(4);
            case D -> exposedField = fields.get(5);
        }
        exposedField.min_max = new Vector2d(min, max);
    }

    @Override
    public String name() {
        return "spatial anchor";
    }

    public void syncClient(){
        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.SYNC_ANIMATION)
                .withBoolean(isRunning)
                .withBoolean(isStatic)
                .build();
        AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
    }

    public void flip(){
        setFlipped(!isFlipped());
    }

    public boolean isFlipped(){
        return getBlockState().getValue(SpatialAnchorBlock.FLIPPED);
    }


    public void setFlipped(boolean flipped) {
        updateBlockState(level, getBlockPos(), getBlockState().setValue(SpatialAnchorBlock.FLIPPED, flipped));
    }

    protected void displayScreen(ServerPlayer player){
        double offset = getAnchorOffset();
        long protocol = getProtocol();
        boolean isRunning = isRunning();
        boolean isStatic = isStatic();
        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.OPEN_SCREEN)
                .withDouble(offset)
                .withLong(protocol)
                .withBoolean(isRunning)
                .withBoolean(isStatic)
                .build();

        AllPackets.sendToPlayer(p, player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == BlockBoundPacketType.OPEN_SCREEN){
            BlockPos pos = packet.getBoundPos();
            double offset = packet.getDoubles().get(0);
            long protocol = packet.getLongs().get(0);
            boolean isRunning = packet.getBooleans().get(0);
            boolean isStatic = packet.getBooleans().get(1);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ScreenOpener.open(
                    new SpatialScreen(pos, offset, protocol, isRunning, isStatic)
            ));
        }
        if(packet.getType() == BlockBoundPacketType.SYNC_ANIMATION){
            isRunning = packet.getBooleans().get(0);
            isStatic = packet.getBooleans().get(1);
        }

    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SETTING){
            double offset = packet.getDoubles().get(0);
            long protocol = packet.getLongs().get(0);
            boolean isRunning = packet.getBooleans().get(0);
            boolean isStatic = packet.getBooleans().get(1);
            setAnchorOffset(offset);
            setProtocol(protocol);
            setRunning(isRunning);
            setStatic(isStatic);
        }
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if(clientPacket)return;
        fields.forEach(e -> compound.put("field_" + e.type.name(), e.serialize()));
        compound.putInt("exposed_field", fields.indexOf(exposedField));
        compound.putBoolean("isRunning", isRunning);
        compound.putBoolean("isStatic", isStatic);
        compound.putDouble("offset", anchorOffset);
        compound.putLong("protocol", protocol);
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

        isRunning = compound.getBoolean("isRunning");
        isStatic = compound.getBoolean("isStatic");
        anchorOffset = compound.getDouble("offset");
        protocol = compound.getLong("protocol");

    }
}
