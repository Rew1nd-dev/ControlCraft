package com.verr1.controlcraft.content.blocks.slider;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.controlcraft.Config;
import com.verr1.controlcraft.content.blocks.ShipConnectorBlockEntity;
import com.verr1.controlcraft.content.cctweaked.peripheral.MotorPeripheral;
import com.verr1.controlcraft.content.cctweaked.peripheral.SliderPeripheral;
import com.verr1.controlcraft.content.gui.SliderScreen;
import com.verr1.controlcraft.content.valkyrienskies.attachments.SliderForceInducer;
import com.verr1.controlcraft.foundation.api.*;
import com.verr1.controlcraft.foundation.data.ShipPhysics;
import com.verr1.controlcraft.foundation.data.SynchronizedField;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.constraint.ConnectContext;
import com.verr1.controlcraft.foundation.data.control.Controller;
import com.verr1.controlcraft.foundation.data.control.PID;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.data.logical.LogicalSlider;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.network.packets.specific.ExposedFieldSyncClientPacket;
import com.verr1.controlcraft.foundation.type.*;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.MathUtils;
import com.verr1.controlcraft.utils.SerializeUtils;
import com.verr1.controlcraft.utils.VSMathUtils;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.joints.*;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;

import java.lang.Math;
import java.util.List;
import java.util.Optional;

// @SuppressWarnings("unused")
public class SliderBlockEntity extends ShipConnectorBlockEntity implements
        IBruteConnectable, IConstraintHolder, IControllerProvider,
        IHaveGoggleInformation, ITerminalDevice, IPacketHandler
{

    private double MAX_SLIDE_DISTANCE = 32;
    public SynchronizedField<Double> controlForce = new SynchronizedField<>(0.0);
    private ConnectContext context = ConnectContext.EMPTY;

    private boolean isLocked = false;

    private final Controller controller = new Controller().setParameter(DEFAULT_POSITION_MODE_PARAMS);

    private TargetMode targetMode = TargetMode.POSITION;
    private LockMode lockMode = LockMode.OFF;
    private CheatMode cheatMode = CheatMode.NONE;

    private final LerpedFloat animatedDistance = LerpedFloat.linear();
    public float animatedTargetDistance = 0;

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    controlForce::read,
                    controlForce::write,
                    "Force",
                    ExposedFieldType.FORCE
            ).withSuggestedRange(0, 1000),
            new ExposedFieldWrapper(
                    () -> this.getController().getTarget(),
                    t -> this.getController().setTarget(t),
                    "target",
                    ExposedFieldType.TARGET
            ).withSuggestedRange(0, 15),
            new ExposedFieldWrapper(
                    () -> this.getController().getTarget(),
                    t -> this.getController().setTarget(t),
                    "target",
                    ExposedFieldType.TARGET$1
            ).withSuggestedRange(0, 15),
            new ExposedFieldWrapper(
                    () -> (isLocked ? 1.0 : 0.0),
                    (d) -> {
                        if(d > (double) 1 / 15)tryLock();
                        else if(d < (double) 1 / 15)tryUnlock();
                    },
                    "Locked",
                    ExposedFieldType.IS_LOCKED
            ),
            new ExposedFieldWrapper(
                    () -> (isLocked ? 1.0 : 0.0),
                    (d) -> {
                        if(d > (double) 1 / 15)tryLock();
                        else if(d < (double) 1 / 15)tryUnlock();
                    },
                    "Locked",
                    ExposedFieldType.IS_LOCKED$1
            )
    );

    private SliderPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new SliderPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public Controller getController() {
        return controller;
    }

    public double getTarget(){
        return controller.getTarget();
    }

    public void setTarget(double target){
        controller.setTarget(target);
    }


    public void setMode(boolean adjustingPosition) {
        targetMode = adjustingPosition ? TargetMode.POSITION : TargetMode.VELOCITY;
        if(adjustingPosition){
            getController().setParameter(IControllerProvider.DEFAULT_POSITION_MODE_PARAMS);
        }else {
            getController().setParameter(IControllerProvider.DEFAULT_VELOCITY_MODE_PARAMS);
        }
    }

    public void toggleMode(){
        setMode(targetMode != TargetMode.POSITION);
    }

    public void setLockMode(boolean softLockMode) {
        lockMode = softLockMode ? LockMode.ON : LockMode.OFF;
        setChanged();
    }

    public void setCheatMode(boolean cheatMode) {
        this.cheatMode = cheatMode ? CheatMode.NO_REPULSE : CheatMode.NONE;
        setChanged();
    }

    public void lock(){
        if(level == null || level.isClientSide)return;

        Direction selfAlign = getAlign();
        Direction selfForward = getForward();
        Direction compAlign = getCompanionShipAlign();
        Ship compShip = getCompanionServerShip();
        if(compShip == null)return;
        long selfId = getShipID();
        long compId = compShip.getId();
        Quaterniondc hingeQuaternion_Own = VSMathUtils.getQuaternionToEast(selfAlign);
        Quaterniondc hingeQuaternion_Cmp = VSMathUtils.getQuaternionToEast(getCompanionShipAlign().getOpposite());

        int sign = compAlign.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;
        Vector3dc absDir = MathUtils.abs(ValkyrienSkies.set(new Vector3d(), compAlign.getNormal()));
        float d = (float) getSlideDistance();

        /*
        VSFixedJoint joint = new VSFixedJoint(
                selfId,
                new VSJointPose(context.self().getPos(), context.self().getRot()),
                compId,
                new VSJointPose(context.comp().getPos().fma(sign * d, absDir, new Vector3d()), context.comp().getRot()),
                new VSJointMaxForceTorque(1e10f, 1e10f)
        );
        * */
        VSDistanceJoint joint = new VSDistanceJoint(
                selfId,
                context.self(),
                compId,
                context.comp(),
                new VSJointMaxForceTorque(1e20f, 1e20f),
                d - 1e-4f,
                d + 1e-4f,
                1e-20f,
                null,
                null
        );

        overrideConstraint("fix", joint);

        isLocked = true;
        setChanged();

    }

    public void unlock(){
        if(level == null || level.isClientSide)return;
        removeConstraint("fix");
        isLocked = false;
        setChanged();
    }

    public void lockCheck(){
        if(lockMode != LockMode.ON)return;
        if(targetMode == TargetMode.POSITION){
            if(Math.abs(getSlideDistance() - getTarget()) < 1e-3){
                tryLock();
            }else{
                tryUnlock();
            }
        }
        if(targetMode == TargetMode.VELOCITY){
            if(Math.abs(getTarget()) < 1e-3){
                tryLock();
            }else{
                tryUnlock();
            }
        }
    }

    public void tryLock(){
        if(isLocked)return;
        lock();
    }

    public void tryUnlock(){
        if(!isLocked)return;
        unlock();
    }

    public double getAnimatedTargetDistance(float partialTicks) {
        return animatedDistance.getValue(partialTicks);
    }

    public Direction getVertical(){
        if(getDirection().getAxis() != Direction.Axis.Y)return Direction.UP;
        return Direction.SOUTH;
    }

    public Quaterniondc getQuaternionOfPlacement(){
        return VSMathUtils.getQuaternionOfPlacement(getDirection());
    }

    public BlockPos getAssembleBlockPos(){
        return getBlockPos().relative(getDirection());
    }

    public Vector3d getAssembleBlockPosJOML(){
        return ValkyrienSkies.set(new Vector3d(), getAssembleBlockPos().getCenter());
    }

    @Override
    public Direction getAlign() {
        return getDirection();
    }

    @Override
    public Direction getForward() {
        return getVertical().getOpposite();
    }

    public double getOutputForce(){
        return controlForce.read();
    }

    public void setOutputForce(double force){
        controlForce.write(force);
    }

    public void assemble(){
        // Only Assemble 1 Block When Being Right-Clicked with wrench. You Should Build Your Ship Up On This Assembled Block, Or Else Use Linker Tool Instead
        if(level == null || level.isClientSide)return;
        Ship self = getShipOn();
        if(self == null)return;
        ServerLevel serverLevel = (ServerLevel) level;
        List<BlockPos> collected = List.of(getAssembleBlockPos());
        Ship comp = ShipAssembler.INSTANCE.assembleToShip(serverLevel, collected, true, 1, true);
        long compId = comp.getId();
        long selfId = self.getId();
        Vector3dc selfContact = getAssembleBlockPosJOML();
        Vector3dc compOffset = comp.getTransform().getPositionInShip();  // new Vector3d();
        Vector3dc selfOffset = self.getTransform().getPositionInShip();  //.sub(selfContact, new Vector3d())

        float m = (float)(getDirection().getAxisDirection() == Direction.AxisDirection.POSITIVE ? MAX_SLIDE_DISTANCE : MAX_SLIDE_DISTANCE);

        Quaterniondc selfQuaternion = VSMathUtils.getQuaternionToEast(getDirection());
        Quaterniondc compQuaternion = VSMathUtils.getQuaternionToEast(getDirection());

        VSPrismaticJoint joint = new VSPrismaticJoint(
                selfId,
                new VSJointPose(selfContact, selfQuaternion),
                compId,
                new VSJointPose(compOffset, compQuaternion),
                new VSJointMaxForceTorque(1e20f, 1e20f),
                new VSD6Joint.LinearLimitPair(-(float) MAX_SLIDE_DISTANCE, (float) MAX_SLIDE_DISTANCE, null, null, null, null)
        );



        recreateConstrains(joint);
        setCompanionShipID(compId);
        setCompanionShipDirection(getDirection().getOpposite());
        setChanged();

    }


    public double getSlideDistance(){
        if(!hasCompanionShip())return 0;
        if(readComp().mass() < 1e-2)return 0;
        Vector3dc own_local_pos = context.self().getPos();
        Vector3dc cmp_local_pos = context.comp().getPos();

        ShipPhysics own_sp = readSelf();
        ShipPhysics cmp_sp = readComp();

        Matrix4dc own_s2w = own_sp.s2wTransform();
        Matrix4dc own_w2s = own_sp.w2sTransform();
        Matrix4dc cmp_s2w = cmp_sp.s2wTransform();

        Vector3dc own_wc = own_s2w.transformPosition(own_local_pos, new Vector3d());
        Vector3dc cmp_wc = cmp_s2w.transformPosition(cmp_local_pos, new Vector3d());
        Vector3dc sub_sc = own_w2s
                .transformDirection(
                        cmp_wc.sub(own_wc, new Vector3d()), new Vector3d()
                );

        Direction dir = getDirection();
        double sign = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;
        double distance = switch (dir.getAxis()){
            case X -> sign * sub_sc.x();
            case Y -> sign * sub_sc.y();
            case Z -> sign * sub_sc.z();
        };

        return distance;
    }

    public void recreateConstrains(VSJoint joint) {
        overrideConstraint("slide", joint);
        updateConnectContext();
    }

    public void updateConnectContext(){
        Optional.ofNullable(getConstraint("slide"))
                .ifPresent(joint -> {
                    VSPrismaticJoint revolute = (VSPrismaticJoint) joint;
                    context = new ConnectContext(revolute.getPose0(), revolute.getPose1());
                });
    }

    public SliderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(() -> cheatMode.name(), n -> cheatMode = CheatMode.valueOf(n.toUpperCase()), SerializeUtils.STRING, "cheatMode"), Side.SERVER);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(() -> targetMode.name(), n -> targetMode = TargetMode.valueOf(n.toUpperCase()), SerializeUtils.STRING, "targetMode"), Side.SERVER);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(() -> lockMode.name(), n -> lockMode = LockMode.valueOf(n.toUpperCase()), SerializeUtils.STRING, "lockMode"), Side.SERVER);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::isLocked, bl -> isLocked = bl, SerializeUtils.BOOLEAN, "isLocked"), Side.SERVER);

        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> fields.forEach(f -> f.deserialize(tag.getCompound("field_" + f.type.name()))),
                        tag -> fields.forEach(e -> tag.put("field_" + e.type.name(), e.serialize()))
                ),
                Side.SERVER
        );
        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> getController().deserialize(tag.getCompound("controller")),
                        tag -> tag.put("controller", getController().serialize())
                ),
                Side.SERVER
        );
        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        $ -> {if(targetMode == TargetMode.VELOCITY)getController().setTarget(0);},
                        $ -> {}
                ),
                Side.SERVER
        );

        registerConstraintKey("slide");
        registerConstraintKey("fix");



        lazyTickRate = 20;
        MAX_SLIDE_DISTANCE = Config.PhysicsMaxSlideDistance;
    }

    @Override
    public void destroyConstraints() {
        removeConstraint("slide");
        removeConstraint("fix");
    }

    @Override
    public void bruteDirectionalConnectWith(BlockPos compPos, Direction compAlign, Direction compForward) {
        if(!VSMathUtils.isVertical(compAlign, compForward))return;
        Direction selfAlign = getAlign();
        Direction selfForward = getForward();
        Ship compShip = ValkyrienSkies.getShipManagingBlock(level, compPos);
        if(compShip == null)return;
        long selfId = getShipID();
        long compId = compShip.getId();

        float m = (float)(selfAlign.getAxisDirection() == Direction.AxisDirection.POSITIVE ? MAX_SLIDE_DISTANCE : MAX_SLIDE_DISTANCE);

        Quaterniondc hingeQuaternion_Own = VSMathUtils.getQuaternionToEast(selfAlign);
        Quaterniondc hingeQuaternion_Cmp = VSMathUtils.getQuaternionOfPlacement(compAlign.getOpposite(), compForward).conjugate();


        Vector3dc asmPos_Own = getAssembleBlockPosJOML();
        Vector3dc asmPos_Asm = ValkyrienSkies.set(new Vector3d(), compPos.getCenter());

        VSPrismaticJoint joint = new VSPrismaticJoint(
                selfId,
                new VSJointPose(asmPos_Own, hingeQuaternion_Own),
                compId,
                new VSJointPose(asmPos_Asm, hingeQuaternion_Cmp),
                new VSJointMaxForceTorque(1e20f, 1e20f),
                new VSD6Joint.LinearLimitPair(-m, m, null, null, null, null)
        );

        recreateConstrains(joint);
        setCompanionShipID(compId);
        setCompanionShipDirection(compAlign);
        setChanged();

    }

    public void syncClient(){
        if(!level.isClientSide){
            var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.SYNC_0)
                    .withDouble(getSlideDistance())
                    .build();
            ControlCraftPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    public void setAnimatedDistance(float d){
        animatedTargetDistance = (float) VSMathUtils.clamp0(d, MAX_SLIDE_DISTANCE);

    }

    public void syncAttachInducer(){
        if(level != null && level.isClientSide)return;

        Optional
            .ofNullable(getLoadedServerShip())
            .map(SliderForceInducer::getOrCreate)
            .ifPresent(inducer -> inducer.alive(WorldBlockPos.of(level, getBlockPos())));

    }


    @Override
    public void tickServer() {
        super.tickServer();
        syncClient();
        syncAttachInducer();
        ExposedFieldSyncClientPacket.syncClient(this, getBlockPos(), level);
    }

    @Override
    public void tickClient() {
        super.tickClient();
        tickAnimation();
    }

    public void tickAnimation(){
        animatedDistance.chase(animatedTargetDistance, 0.5, LerpedFloat.Chaser.EXP);
        animatedDistance.tickChaser();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return ITerminalDevice.super.TerminalDeviceToolTip(tooltip, isPlayerSneaking);
    }

    public LogicalSlider getLogicalSlider() {
        if(level.isClientSide)return null;
        ServerShip ship = getCompanionServerShip();
        if(ship == null)return null;

        return new LogicalSlider(
                getShipID(),
                getCompanionShipID(),
                WorldBlockPos.of(level, getBlockPos()),
                getAlign(),
                context.self().getPos(),
                context.comp().getPos(),
                targetMode == TargetMode.POSITION,
                cheatMode != CheatMode.NO_REPULSE,
                !isLocked(),
                getOutputForce(),
                getController()
        );
    }

    public boolean isLocked() {
        return isLocked;
    }

    protected void displayScreen(ServerPlayer player){

        double t = getController().getTarget();
        double v = getSlideDistance();

        boolean m = targetMode == TargetMode.POSITION;
        boolean l = isLocked();
        boolean c = cheatMode == CheatMode.NO_REPULSE;


        PID pidParams = getController().getPIDParams();

        var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.OPEN_SCREEN_0)
                .withDouble(t)
                .withDouble(v)
                .withDouble(pidParams.p())
                .withDouble(pidParams.i())
                .withDouble(pidParams.d())
                .withBoolean(m)
                .withBoolean(l)
                .withBoolean(c)
                .build();

        ControlCraftPackets.sendToPlayer(p, player);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == RegisteredPacketType.OPEN_SCREEN_0){
            double t = packet.getDoubles().get(0);
            double v = packet.getDoubles().get(1);
            double p = packet.getDoubles().get(2);
            double i = packet.getDoubles().get(3);
            double d = packet.getDoubles().get(4);
            boolean m = packet.getBooleans().get(0);
            boolean l = packet.getBooleans().get(1);
            boolean c = packet.getBooleans().get(2);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ScreenOpener.open(new SliderScreen(getBlockPos(), p, i, d, v, t, m, l, c)));
        }
        if(packet.getType() == RegisteredPacketType.SYNC_0){
            setAnimatedDistance(packet.getDoubles().get(0).floatValue());
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {

        if(packet.getType() == RegisteredPacketType.TOGGLE_0){
            setCheatMode(cheatMode == CheatMode.NONE);
        }
        if(packet.getType() == RegisteredPacketType.TOGGLE_1){
            setLockMode(lockMode == LockMode.OFF);
        }
    }


    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    public String name() {
        return "slider";
    }
}
