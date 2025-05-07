package com.verr1.controlcraft.content.blocks.spatial;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.verr1.controlcraft.Config;
import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.content.blocks.SharedKeys;
import com.verr1.controlcraft.content.gui.layouts.api.IScheduleProvider;
import com.verr1.controlcraft.content.valkyrienskies.attachments.SpatialForceInducer;
import com.verr1.controlcraft.foundation.api.*;
import com.verr1.controlcraft.foundation.api.delegate.ITerminalDevice;
import com.verr1.controlcraft.foundation.api.operatable.IBruteConnectable;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.network.executors.ClientBuffer;
import com.verr1.controlcraft.foundation.network.executors.CompoundTagPort;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.content.cctweaked.peripheral.SpatialAnchorPeripheral;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.control.SpatialSchedule;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.data.logical.LogicalSpatial;
import com.verr1.controlcraft.foundation.managers.SpatialLinkManager;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.network.packets.specific.ExposedFieldSyncClientPacket;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.MinecraftUtils;
import com.verr1.controlcraft.utils.SerializeUtils;
import com.verr1.controlcraft.utils.VSGetterUtils;
import com.verr1.controlcraft.utils.VSMathUtils;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;

import java.util.List;
import java.util.Optional;

import static com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;
import static com.verr1.controlcraft.content.gui.layouts.api.ISerializableSchedule.SCHEDULE;

public class SpatialAnchorBlockEntity extends OnShipBlockEntity implements
        IBruteConnectable, ITerminalDevice, IPacketHandler, IScheduleProvider, IHaveGoggleInformation
{
    public static NetworkKey IS_RUNNING = NetworkKey.create("is_running");
    public static NetworkKey IS_STATIC = NetworkKey.create("is_static");
    public static NetworkKey OFFSET = NetworkKey.create("offset");
    public static NetworkKey PROTOCOL = NetworkKey.create("protocol");

    private boolean isRunning = false;
    private ISpatialTarget tracking = null;
    private boolean isStatic = false;


    private double anchorOffset = 2.0;
    private long protocol = 0;
    private final int MAX_DISTANCE_SQRT_CAN_LINK = Config.MaxDistanceSpatialCanLink * Config.MaxDistanceSpatialCanLink;
    private SpatialSchedule schedule = new SpatialSchedule().withPPID(18, 3, 12, 10);

    private SpatialAnchorPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    this::getAnchorOffset,
                    this::setAnchorOffset,
                    "offset",
                    ExposedFieldType.ANCHOR_OFFSET
            ).withSuggestedRange(0, 16),
            new ExposedFieldWrapper(
                    () -> (double) (isRunning() ? 1 : 0),
                    v -> setRunning(v > (double) 1 / 15) ,
                    "running",
                    ExposedFieldType.IS_RUNNING
            ),
            new ExposedFieldWrapper(
                    () -> (double)(isStatic() ? 0 : 1),
                    v -> setStatic(v > (double) 1 / 15),
                    "dynamic",
                    ExposedFieldType.IS_STATIC
            )
    );


    public SpatialSchedule getSchedule() {
        return schedule;
    }


    public boolean isRunning() {
        return isRunning;
    }

    public ISpatialTarget getTracking() {
        return tracking;
    }

    public LogicalSpatial getLogicalSpatial(){
        return new LogicalSpatial(
                WorldBlockPos.of(level, getBlockPos()),
                getAlign(),
                getForward(),
                getShipOrGroundID(),
                getDimensionID(),
                shouldDrive(),
                isStatic,
                protocol,
                getSchedule()
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
        Direction facing = getBlockState().getValue(FACING);
        Boolean align = getBlockState().getValue(AXIS_ALONG_FIRST_COORDINATE);
        if(facing.getAxis() != Direction.Axis.X){
            if(align)return Direction.EAST;
            return facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        }
        if(align)return Direction.UP;
        return Direction.SOUTH;
    }


    public void activateWhenRunning(){
        if(!isRunning())return;
        if(level.isClientSide)return;
        SpatialLinkManager.activate(getLogicalSpatial());
    }

    public void trackNearestWhenRunning(){
        if(!isRunning())return;
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
        if(level == null || level.isClientSide)return;
        if(!isOnShip())return;
        if(getTracking() == null || getTracking().pos() == getBlockPos() || isStatic)return;


        updateSchedule(tracking);
    }

    public boolean filter(ISpatialTarget tracking){
        if(level == null || level.isClientSide)return false;
        if(tracking == null)return false;
        if(tracking.pos() == getBlockPos())return false;
        if(!getDimensionID().equals(tracking.dimensionID()))return false;
        if(tracking.shipID() == getShipOrGroundID())return false;
        if(tracking
                .vPos()
                .sub(VSGetterUtils.getAbsolutePosition(WorldBlockPos.of(level, getBlockPos())), new Vector3d())
                .lengthSquared() > MAX_DISTANCE_SQRT_CAN_LINK)return false;

        return true;
    }

    private void updateSchedule(ISpatialTarget spatial){
        if(level == null || level.isClientSide)return;
        LoadedServerShip ship = getLoadedServerShip();
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

        Vector3dc dir = ValkyrienSkies.set(new Vector3d(), c_align.getNormal()).mul(anchorOffset);
        Vector3dc cFace_sc = ValkyrienSkies.set(new Vector3d(), c_pos).add(dir);
        Vector3dc cCenter_sc = ship.getInertiaData().getCenterOfMassInShip();
        Vector3dc relative_r_sc = new Vector3d(cFace_sc).sub(cCenter_sc, new Vector3d());

        Vector3dc relative_r_wc = q_target.transform(relative_r_sc, new Vector3d());
        Vector3dc tFace_wc = spatial.vPos();

        Vector3dc p_target = new Vector3d(tFace_wc).sub(relative_r_wc, new Vector3d());

        schedule.overrideTarget(q_target, p_target);
    }

    public void syncAttachedInducer(){
        if(level == null || level.isClientSide)return;

        Optional.ofNullable(getLoadedServerShip())
                .map(SpatialForceInducer::getOrCreate)
                .ifPresent(inducer -> inducer.replace(
                        WorldBlockPos.of(level, getBlockPos()),
                        this::getLogicalSpatial
                ));

    }

    @Override
    public void tickServer() {
        super.tickServer();
        activateWhenRunning();
        trackNearestWhenRunning();
        syncAttachedInducer();
        updateSchedule();
        syncForNear(true, IS_STATIC, IS_RUNNING, FIELD);
        // syncClient();
    }

    @Override
    public void bruteDirectionalConnectWith(BlockPos pos, Direction align, Direction forward) {
        if(level == null || level.isClientSide)return;
        // just make a dummy
        tracking = new LogicalSpatial(
                WorldBlockPos.of(level, getBlockPos()),
                align,
                forward,
                VSGetterUtils.getLoadedServerShip((ServerLevel)level ,pos).map(LoadedServerShip::getId).orElse(-1L),
                getDimensionID(),
                true,
                true,
                protocol,
                new SpatialSchedule()
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

    /*
    @Override
    public void setExposedField(ExposedFieldType type, double min, double max, ExposedFieldDirection openTo) {
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
    * */

    @Override
    public void lazyTickServer() {
        super.lazyTickServer();
        syncForNear(true, FIELD);
    }

    @Override
    public String name() {
        return "spatial anchor";
    }


    public void flip(){
        setFlipped(!isFlipped());
    }

    public boolean isFlipped(){
        return getBlockState().getValue(SpatialAnchorBlock.FLIPPED);
    }


    public void setFlipped(boolean flipped) {
        MinecraftUtils.updateBlockState(level, getBlockPos(), getBlockState().setValue(SpatialAnchorBlock.FLIPPED, flipped));
    }


    public SpatialAnchorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

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

        buildRegistry(SCHEDULE)
                .withBasic(CompoundTagPort.of(
                        () -> schedule.serialize(),
                        tag -> schedule.deserialize(tag)
                ))
                .withClient(
                        new ClientBuffer<>(SerializeUtils.UNIT, CompoundTag.class)
                )
                .dispatchToSync()
                .register();

        buildRegistry(IS_RUNNING).withBasic(SerializePort.of(this::isRunning, this::setRunning, SerializeUtils.BOOLEAN)).withClient(ClientBuffer.BOOLEAN.get()).dispatchToSync().register();
        buildRegistry(IS_STATIC).withBasic(SerializePort.of(this::isStatic, this::setStatic, SerializeUtils.BOOLEAN)).withClient(ClientBuffer.BOOLEAN.get()).dispatchToSync().register();
        buildRegistry(OFFSET).withBasic(SerializePort.of(this::getAnchorOffset, this::setAnchorOffset, SerializeUtils.DOUBLE)).withClient(ClientBuffer.DOUBLE.get()).register();
        buildRegistry(PROTOCOL).withBasic(SerializePort.of(this::getProtocol, this::setProtocol, SerializeUtils.LONG)).withClient(ClientBuffer.LONG.get()).register();


        /*
        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> ITerminalDevice.super.deserialize(tag.getCompound("fields")),
                        tag -> tag.put("fields", ITerminalDevice.super.serialize()),
                        FIELD
                ),
                Side.SHARED
        );
        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> schedule.deserialize(tag.getCompound("schedule")),
                        tag -> tag.put("schedule", schedule.serialize()),
                        SCHEDULE
                ),
                Side.SHARED
        );
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::isRunning, this::setRunning, SerializeUtils.BOOLEAN, IS_RUNNING), Side.SHARED);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::isStatic, this::setStatic, SerializeUtils.BOOLEAN, IS_STATIC), Side.SHARED);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::getAnchorOffset, this::setAnchorOffset, SerializeUtils.DOUBLE, OFFSET), Side.SHARED);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::getProtocol, this::setProtocol, SerializeUtils.LONG, PROTOCOL), Side.SHARED);
        *
        * */

    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return ITerminalDevice.super.TerminalDeviceToolTip(tooltip, isPlayerSneaking);
    }

}
