package com.verr1.controlcraft.content.blocks.motor;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.controlcraft.content.blocks.ShipConnectorBlockEntity;
import com.verr1.controlcraft.content.cctweaked.peripheral.MotorPeripheral;
import com.verr1.controlcraft.content.valkyrienskies.attachments.MotorForceInducer;
import com.verr1.controlcraft.foundation.api.IBruteConnectable;
import com.verr1.controlcraft.foundation.api.IControllerProvider;
import com.verr1.controlcraft.foundation.api.IPacketHandler;
import com.verr1.controlcraft.foundation.api.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.*;
import com.verr1.controlcraft.foundation.data.constraint.ConnectContext;
import com.verr1.controlcraft.foundation.data.control.Controller;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.data.logical.LogicalMotor;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.joints.*;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.assembly.ShipAssembler;

import javax.annotation.Nullable;
import java.lang.Math;
import java.util.List;
import java.util.Optional;

public abstract class AbstractMotorBlockEntity extends ShipConnectorBlockEntity
    implements
        IBruteConnectable, IHaveGoggleInformation, IControllerProvider,
        ITerminalDevice, IPacketHandler
{
    private float animatedAngle = 0;
    private final LerpedFloat animatedLerpedAngle = LerpedFloat.angular();
    public SynchronizedField<Double> controlTorque = new SynchronizedField<>(0.0);

    private final Controller controller = new Controller().setParameter(DEFAULT_VELOCITY_MODE_PARAMS);
    private ConnectContext context = ConnectContext.EMPTY;

    private boolean isLocked = false;

    private double offset = 0;
    private TargetMode targetMode = TargetMode.VELOCITY;
    private LockMode lockMode = LockMode.OFF;
    private CheatMode cheatMode = CheatMode.NONE;
    private boolean reverseCreateInput = false;

    public void setTargetAccordingly(double target){
        switch (targetMode){
            case POSITION, FORCED_POSITION : controller.setTarget(MathUtils.clamp(target, Math.PI));
            case VELOCITY, FORCED_VELOCITY, POWER : controller.setTarget(target);
        }
        setChanged();
    }

    public void setLockMode(LockMode lockMode) {
        this.lockMode = lockMode;
        if(lockMode == LockMode.OFF)tryUnlock();
        setChanged();
    }

    public void setCheatMode(CheatMode cheatMode) {
        this.cheatMode = cheatMode;
        setChanged();
    }

    public void setReverseCreateInput(boolean reverseCreateInput) {
        this.reverseCreateInput = reverseCreateInput;
        setChanged();
    }

    public void setOffset(double offset) {
        this.offset = offset;
        setChanged();
    }

    public double getOffset() {return offset;}
    public TargetMode getTargetMode() {return targetMode;}
    public LockMode getLockMode() {return lockMode;}
    public CheatMode getCheatMode() {return cheatMode;}
    public boolean isReverseCreateInput() {return reverseCreateInput;}
    public double getTarget(){return controller.getTarget();}
    public boolean isLocked() {return isLocked;}

    public void setTargetMode(TargetMode targetMode) {
        this.targetMode = targetMode;
        if(targetMode == TargetMode.POSITION){
            controller.setParameter(DEFAULT_POSITION_MODE_PARAMS);
        }
        if(targetMode == TargetMode.VELOCITY){
            controller.setParameter(DEFAULT_VELOCITY_MODE_PARAMS);
        }
        setChanged();
    }

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    controlTorque::read,
                    controlTorque::write,
                    "Torque",
                    ExposedFieldType.TORQUE
            ).withSuggestedRange(0, 1000),
            new ExposedFieldWrapper(
                    this::getTarget,
                    this::setTargetAccordingly,
                    "target",
                    ExposedFieldType.TARGET
            ).withSuggestedRange(0, Math.PI / 2),
            new ExposedFieldWrapper(
                    this::getTarget,
                    this::setTargetAccordingly,
                    "target",
                    ExposedFieldType.TARGET$1
            ).withSuggestedRange(0, Math.PI / 2),
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

    private MotorPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new MotorPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    public String name() {
        return "motor";
    }

    public void tryLock(){
        if(isLocked)return;
        isLocked = true;
        lock();
    }

    private void lock(){
        if(level == null || level.isClientSide)return;
        if(!hasCompanionShip())return;

        Quaterniondc hingeQuaternion_Own = new
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(getServoDirection()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();


        double AngleFix = VSMathUtils.getDumbFixOfLockMode(getServoDirection(), getCompanionShipAlign());

        Quaterniondc hingeQuaternion_Cmp = new Quaterniond()
                .rotateAxis(AngleFix - getServoAngle(), getCompanionShipDirectionJOML())  // dumbFix +  dumb fixing getServoDirectionJOML()
                .mul(VSMathUtils.getQuaternionOfPlacement(getCompanionShipAlign().getOpposite()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();

        Vector3dc v_own = hingeQuaternion_Own.transform(new Vector3d(0, 1, 0));
        Vector3dc v_cmp = hingeQuaternion_Cmp.transform(new Vector3d(0, 1, 0));

        VSFixedJoint joint = new VSFixedJoint(
                getShipID(),
                new VSJointPose(context.self().getPos(), hingeQuaternion_Own),
                getCompanionShipID(),
                new VSJointPose(context.comp().getPos(), hingeQuaternion_Cmp),
                new VSJointMaxForceTorque(1e20f, 1e20f)
        );

        // removeConstraint("revolute");
        overrideConstraint("fix", joint);
        isLocked = true;
        setChanged();
    }




    public void tryUnlock(){
        if(!isLocked)return;
        isLocked = false;
        unlock();
    }

    private void unlock(){
        if(level == null || level.isClientSide)return;
        removeConstraint("fix");
        isLocked = false;
        setChanged();
    }

    public @Nullable LogicalMotor getLogicalMotor(){
        if(level == null || level.isClientSide)return null;
        if(!hasCompanionShip() || !isOnShip())return null;
        return new LogicalMotor(
                getShipID(),
                getCompanionShipID(),
                WorldBlockPos.of(level, getBlockPos()),
                getServoDirection(),
                getCompanionShipAlign(),
                targetMode == TargetMode.POSITION,
                cheatMode == CheatMode.NONE,
                !isLocked(),
                controlTorque.read(),
                getController()
        );
    }

    public void syncAttachInducer(){
        if(level != null && level.isClientSide)return;
        Optional
            .ofNullable(getLoadedServerShip())
            .map(MotorForceInducer::getOrCreate)
            .ifPresent(inducer -> inducer.alive(WorldBlockPos.of(level, getBlockPos())));
    }

    @Override
    public void tickServer() {
        super.tickServer();
        syncAttachInducer();
        syncClient();
        lockCheck();
        ExposedFieldSyncClientPacket.syncClient(this, getBlockPos(), level);
    }

    @Override
    public void tickClient() {
        super.tickClient();
        tickAnimation();
    }

    public void tickAnimation(){
        animatedLerpedAngle.chase(Math.toDegrees(animatedAngle), 0.5, LerpedFloat.Chaser.EXP);
        animatedLerpedAngle.tickChaser();
    }

    public void syncClient(){
        if(level != null && !level.isClientSide){
            var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.SYNC_0)
                    .withDouble(getServoAngle())
                    .build();
            ControlCraftPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    public void lockCheck(){
        if(lockMode != LockMode.ON)return;
        if(targetMode == TargetMode.POSITION){
            if(Math.abs(getServoAngle() - getTarget()) < 1e-3){
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

    @Override
    public Controller getController() {return controller;}

    @Override
    public Direction getAlign() {return getDirection();}

    @Override
    public Direction getForward() {return getServoDirection().getOpposite();}
    /*
     * @returns:  indicates the positive rotational direction vector in ship-coordinate
     * */
    public abstract Direction getServoDirection();

    public Vector3d getServoDirectionJOML(){
        return ValkyrienSkies.set(new Vector3d(), getServoDirection().getNormal());
    }

    public abstract BlockPos getAssembleBlockPos();

    public abstract Vector3d getRotationCenterPosJOML();

    public Quaterniondc getQuaternionOfPlacement(){return VSMathUtils.getQuaternionOfPlacement(getServoDirection());}

    public double getServoAngle(){
        if(!hasCompanionShip())return 0;
        Matrix3dc own = readSelf().rotationMatrix().transpose(new Matrix3d()); //wc2sc
        Matrix3dc cmp = readComp().rotationMatrix().transpose(new Matrix3d());
        return VSMathUtils.get_yc2xc(own, cmp, getServoDirection(), getCompanionShipAlign());
    }

    public double getServoAngularSpeed(){
        if(!hasCompanionShip())return 0;
        ShipPhysics own = readSelf();
        ShipPhysics cmp = readComp();

        Matrix3dc m_own = own.rotationMatrix().transpose(new Matrix3d()); //wc2sc
        Vector3dc w_own = own.omega();
        Vector3dc w_cmp = cmp.omega();
        return VSMathUtils.get_dyc2xc(m_own, w_own, w_cmp,  getServoDirection(), getCompanionShipAlign());
    }

    public void setOutputTorque(double torque){
        controlTorque.write(torque);
    }

    public double getOutputTorque(){
        return controlTorque.read();
    }

    public void destroy() {
        super.destroy();
        if(level.isClientSide)return;
        destroyConstraints();
    }

    @Override
    public void destroyConstraints() {
        removeConstraint("revolute");
        removeConstraint("fix");
    }

    public void setMode(boolean adjustAngle){
        targetMode = adjustAngle ? TargetMode.POSITION : TargetMode.VELOCITY;
        if(adjustAngle){
            getController().setParameter(DEFAULT_POSITION_MODE_PARAMS);
        }else{
            getController().setParameter(DEFAULT_VELOCITY_MODE_PARAMS);
        }
        setChanged();
    }

    public void toggleMode(){
        setMode(!(targetMode == TargetMode.POSITION));
    }

    public void assemble(){
        if(level == null || level.isClientSide)return;
        Ship self = getShipOn();
        if(self == null)return;
        ServerLevel serverLevel = (ServerLevel) level;
        List<BlockPos> collected = List.of(getAssembleBlockPos());
        Ship comp = ShipAssembler.INSTANCE.assembleToShip(serverLevel, collected, true, 1, true);
        long compId = comp.getId();
        long selfId = self.getId();
        Vector3dc selfContact = getRotationCenterPosJOML();
        Vector3dc compOffset = comp.getTransform().getPositionInShip();  // new Vector3d();
        Vector3dc selfOffset = self.getTransform().getPositionInShip(); //.sub(selfContact, new Vector3d())
        Quaterniondc selfQuaternion = VSMathUtils.getQuaternionToEast(getServoDirection());
        Quaterniondc compQuaternion = new Quaterniond(selfQuaternion);


        Vector3d direction = getServoDirectionJOML();

        VSRevoluteJoint joint = new VSRevoluteJoint(
            selfId,
            new VSJointPose(selfContact, selfQuaternion),
            compId,
            new VSJointPose(compOffset, compQuaternion),
            new VSJointMaxForceTorque(1e20f, 1e20f),
            null, null, null, null, null
        );

        recreateConstraints(joint);
        setCompanionShipID(compId);
        setCompanionShipDirection(getServoDirection().getOpposite());
        setStartingAngleOfCompanionShip();
        setChanged();
    }

    public void updateConnectContext(){
        Optional.ofNullable(getConstraint("revolute")).ifPresent(joint -> {
            VSRevoluteJoint revolute = (VSRevoluteJoint) joint;
            context = new ConnectContext(revolute.getPose0(), revolute.getPose1());
        });
    }

    public void recreateConstraints(VSJoint joint){
        overrideConstraint("revolute", joint);
        updateConnectContext();
    }

    @Override
    public void bruteDirectionalConnectWith(BlockPos assemPos, Direction assemDir, Direction forward) {
        Direction servoDir = getServoDirection();
        Vector3dc ownDir = getServoDirectionJOML();
        Vector3dc asmDir = ValkyrienSkies.set(new Vector3d(), assemDir.getNormal());

        Ship compShip = ValkyrienSkies.getShipManagingBlock(level, assemPos);
        if(compShip == null)return;
        long selfShipID = getShipID();
        long compShipId = compShip.getId();
        Quaterniondc hingeQuaternion_Own = VSMathUtils.getQuaternionToEast(servoDir);
        Quaterniondc hingeQuaternion_Cmp = VSMathUtils.getQuaternionToEast(assemDir.getOpposite());

        Vector3dc asmPos_Own = getRotationCenterPosJOML();
        Vector3dc asmPos_Asm = ValkyrienSkies.set(new Vector3d(), assemPos.getCenter());

        VSRevoluteJoint joint = new VSRevoluteJoint(
            selfShipID,
            new VSJointPose(asmPos_Own, hingeQuaternion_Own),
            compShipId,
            new VSJointPose(asmPos_Asm, hingeQuaternion_Cmp),
            new VSJointMaxForceTorque(1e20f, 1e20f),
            null, null, null, null, null
        );

        recreateConstraints(joint);
        setCompanionShipID(compShipId);
        setCompanionShipDirection(assemDir);
        setStartingAngleOfCompanionShip();
        setChanged();

    }

    public AbstractMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);


        registerConstraintKey("revolute");
        registerConstraintKey("fix");

        registerFieldReadWriter(SerializeUtils.ReadWriter.of(() -> getCheatMode().name(), n -> setCheatMode(CheatMode.valueOf(n.toUpperCase())), SerializeUtils.STRING, "cheatMode"), Side.SERVER);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(() -> getTargetMode().name(), n -> setTargetMode(TargetMode.valueOf(n.toUpperCase())), SerializeUtils.STRING, "targetMode"), Side.SERVER);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(() -> getLockMode().name(), n -> setLockMode(LockMode.valueOf(n.toUpperCase())), SerializeUtils.STRING, "lockMode"), Side.SERVER);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::getOffset, this::setOffset, SerializeUtils.DOUBLE, "offset"), Side.SERVER);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(() -> reverseCreateInput, b -> reverseCreateInput = b, SerializeUtils.BOOLEAN, "reverseCreateInput"), Side.SERVER);
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
    }

    public float getAnimatedAngle(float partialTicks) {
        return animatedLerpedAngle.getValue(partialTicks);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == RegisteredPacketType.SYNC_0){
            double angle = packet.getDoubles().get(0);
            animatedAngle = (float) angle;
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == RegisteredPacketType.SETTING_0){
            setOffset(packet.getDoubles().get(0));
        }
        if(packet.getType() == RegisteredPacketType.TOGGLE_0){
            setCheatMode(cheatMode == CheatMode.NONE ? CheatMode.NO_REPULSE : CheatMode.NONE);
        }
        if(packet.getType() == RegisteredPacketType.TOGGLE_1){
            setReverseCreateInput(!reverseCreateInput);
        }
        if(packet.getType() == RegisteredPacketType.TOGGLE_2){
            setLockMode(lockMode == LockMode.ON ? LockMode.OFF : LockMode.ON);
        }
    }

    public void setStartingAngleOfCompanionShip(){
        Ship asm = getCompanionServerShip();
        Ship own = getShipOn();
        if(asm == null)return;
        if(targetMode != TargetMode.POSITION)return;
        double startAngle = VSMathUtils.get_yc2xc(own, asm, getServoDirection(), getCompanionShipAlign());
        getController().setTarget(startAngle);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return ITerminalDevice.super.TerminalDeviceToolTip(tooltip, isPlayerSneaking);
    }

}
