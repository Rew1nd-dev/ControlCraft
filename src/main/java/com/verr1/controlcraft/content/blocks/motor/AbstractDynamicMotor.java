package com.verr1.controlcraft.content.blocks.motor;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.content.blocks.SharedKeys;
import com.verr1.controlcraft.foundation.network.executors.ClientBuffer;
import com.verr1.controlcraft.foundation.network.executors.CompoundTagPort;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.foundation.type.Side;
import com.verr1.controlcraft.content.cctweaked.peripheral.DynamicMotorPeripheral;
import com.verr1.controlcraft.content.valkyrienskies.attachments.DynamicMotorForceInducer;
import com.verr1.controlcraft.foundation.api.IControllerProvider;
import com.verr1.controlcraft.foundation.api.IPacketHandler;
import com.verr1.controlcraft.foundation.api.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.*;
import com.verr1.controlcraft.foundation.data.control.DynamicController;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.data.logical.LogicalDynamicMotor;
import com.verr1.controlcraft.foundation.type.descriptive.CheatMode;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.descriptive.LockMode;
import com.verr1.controlcraft.foundation.type.descriptive.TargetMode;
import com.verr1.controlcraft.utils.MathUtils;
import com.verr1.controlcraft.utils.SerializeUtils;
import com.verr1.controlcraft.utils.VSMathUtils;
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
import org.joml.*;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;

import javax.annotation.Nullable;
import java.lang.Math;
import java.util.List;
import java.util.Optional;

import static com.verr1.controlcraft.content.blocks.SharedKeys.*;

public abstract class AbstractDynamicMotor extends AbstractMotor implements
        IHaveGoggleInformation, IControllerProvider, ITerminalDevice, IPacketHandler
{
    public static NetworkKey REVERSE_CREATE_INPUT = NetworkKey.create("reverse_create_input");


    public SynchronizedField<Double> controlTorque = new SynchronizedField<>(0.0);

    private final DynamicController controller = new DynamicController().withPID(DEFAULT_VELOCITY_MODE_PARAMS);
    private boolean isLocked = false;


    protected TargetMode targetMode = TargetMode.VELOCITY;
    protected LockMode lockMode = LockMode.OFF;
    protected CheatMode cheatMode = CheatMode.NONE;
    protected boolean reverseCreateInput = false;

    public void setTargetAccordingly(double target){
        switch (targetMode){
            case POSITION : controller.setTarget(MathUtils.clamp(target, Math.PI));
            case VELOCITY : controller.setTarget(target);
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

    public TargetMode getTargetMode() {return targetMode;}
    public LockMode getLockMode() {return lockMode;}
    public CheatMode getCheatMode() {return cheatMode;}
    public boolean isReverseCreateInput() {return reverseCreateInput;}
    public double getTarget(){return controller.getTarget();}
    public boolean isLocked() {return isLocked;}

    public void setTargetMode(TargetMode targetMode) {
        if(this.targetMode == targetMode)return;
        this.targetMode = targetMode;
        // delay this because client screen will also call to set PID values of last mode
        Runnable task = () -> {if(targetMode == TargetMode.POSITION){
            controller.PID(DEFAULT_POSITION_MODE_PARAMS);
        }
        if(targetMode == TargetMode.VELOCITY){
            controller.PID(DEFAULT_VELOCITY_MODE_PARAMS);
        }};

        if(level == null || level.isClientSide)return;
        ControlCraftServer.SERVER_DEFERRAL_EXECUTOR.executeLater(task, 1);
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

    private DynamicMotorPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new DynamicMotorPeripheral(this);
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
        if(noCompanionShip() || context.isDirty())return;

        Quaterniondc q_self = new
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(getServoDirection()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();


        double AngleFix = VSMathUtils.getDumbFixOfLockMode(getServoDirection(), getCompanionShipAlign());

        Quaterniondc q_comp = new Quaterniond()
                .rotateAxis(AngleFix - getServoAngle(), getCompanionShipAlignJOML())  // dumbFix +  dumb fixing getServoDirectionJOML()
                .mul(VSMathUtils.getQuaternionOfPlacement(getCompanionShipAlign().getOpposite()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();

        Vector3dc v_own = q_self.transform(new Vector3d(0, 1, 0));
        Vector3dc v_cmp = q_comp.transform(new Vector3d(0, 1, 0));

        /*
        * VSFixedJoint joint = new VSFixedJoint(
                getShipID(),
                new VSJointPose(context.self().getPos(), q_self),
                getCompanionShipID(),
                new VSJointPose(context.comp().getPos(), q_comp),
                new VSJointMaxForceTorque(1e20f, 1e20f)
        );
        * */

        VSAttachmentConstraint fixed = new VSAttachmentConstraint(
                getShipOrGroundID(),
                getCompanionShipID(),
                1.0E-10,
                context.self().getPos().add(v_own, new Vector3d()),
                context.comp().getPos().add(v_cmp, new Vector3d()),
                1.0E10,
                0.0
        );

        updateConstraint("fix", fixed);
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

    public @Nullable LogicalDynamicMotor getLogicalMotor(){
        if(level == null || level.isClientSide)return null;
        if(noCompanionShip())return null;
        return new LogicalDynamicMotor(
                getShipOrGroundID(),
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
        if(level == null || level.isClientSide)return;
        Optional
            .ofNullable(getCompanionServerShip())
            .map(DynamicMotorForceInducer::getOrCreate)
            .ifPresent(inducer -> inducer.alive(WorldBlockPos.of(level, getBlockPos())));
    }

    @Override
    public void tickServer() {
        super.tickServer();
        syncAttachInducer();
        syncForNear(true, FIELD);
        lockCheck();
        // ExposedFieldSyncClientPacket.syncClient(this, getBlockPos(), level);
    }


    @Override
    public void lazyTickServer() {
        super.lazyTickServer();
        syncForNear(true, FIELD, CONTROLLER);
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
    public DynamicController getController() {return controller;}



    public void setOutputTorque(double torque){
        controlTorque.write(torque);
    }

    public double getOutputTorque(){
        return controlTorque.read();
    }

    @Override
    public void destroyConstraints() {
        super.destroyConstraints();
        removeConstraint("fix");
    }

    public void setMode(boolean adjustAngle){
        targetMode = adjustAngle ? TargetMode.POSITION : TargetMode.VELOCITY;
        // delay a bit because client screen will also call to set PID values of last mode
        Runnable task = () -> {if(adjustAngle){
            getController().PID(DEFAULT_POSITION_MODE_PARAMS);
        }else{
            getController().PID(DEFAULT_VELOCITY_MODE_PARAMS);
        }};

        if(level != null && !level.isClientSide) ControlCraftServer.SERVER_DEFERRAL_EXECUTOR.executeLater(task, 1);

        setChanged();
    }

    public void toggleMode(){
        setMode(!(targetMode == TargetMode.POSITION));
    }


    public AbstractDynamicMotor(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        registerConstraintKey("fix");

        buildRegistry(CHEAT_MODE)
                .withBasic(SerializePort.of(
                        this::getCheatMode,
                        this::setCheatMode,
                        SerializeUtils.ofEnum(CheatMode.class)))
                .withClient(ClientBuffer.of(CheatMode.class))
                .register();

        buildRegistry(TARGET_MODE)
                .withBasic(SerializePort.of(this::getTargetMode, this::setTargetMode, SerializeUtils.ofEnum(TargetMode.class)))
                .withClient(ClientBuffer.of(TargetMode.class))
                .register();

        buildRegistry(LOCK_MODE)
                .withBasic(SerializePort.of(this::getLockMode, this::setLockMode, SerializeUtils.ofEnum(LockMode.class)))
                .withClient(ClientBuffer.of(LockMode.class))
                .register();

        buildRegistry(IS_LOCKED)
                .withBasic(SerializePort.of(this::isLocked, bl -> isLocked = bl, SerializeUtils.BOOLEAN))
                .withClient(ClientBuffer.BOOLEAN.get())
                .register();

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

        buildRegistry(CONTROLLER)
                .withBasic(CompoundTagPort.of(
                        () -> getController().serialize(),
                        tag -> getController().deserialize(tag)
                ))
                .withClient(
                        new ClientBuffer<>(SerializeUtils.UNIT, CompoundTag.class)
                )
                .register();

        buildRegistry(TARGET)
                .withBasic(SerializePort.of(
                        () -> getController().getTarget(),
                        t -> getController().setTarget(t),
                        SerializeUtils.DOUBLE
                ))
                .withClient(
                        new ClientBuffer<>(SerializeUtils.DOUBLE, Double.class)
                )
                .register();

        buildRegistry(VALUE)
                .withBasic(SerializePort.of(
                        () -> getController().getValue(),
                        $ -> {},
                        SerializeUtils.DOUBLE
                ))
                .withClient(
                        new ClientBuffer<>(SerializeUtils.DOUBLE, Double.class)
                )
                .runtimeOnly()
                .register();

        buildRegistry(PLACE_HOLDER)
                .withBasic(CompoundTagPort.of(
                        CompoundTag::new,
                        $ ->  {if(targetMode == TargetMode.VELOCITY)getController().setTarget(0);}
                ))
                .register();

        /*



        registerFieldReadWriter(SerializeUtils.ReadWriter.of(() -> getCheatMode().name(), n -> setCheatMode(CheatMode.valueOf(n.toUpperCase())), SerializeUtils.STRING, SharedKeys.CHEAT_MODE), Side.SHARED);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(() -> getTargetMode().name(), n -> setTargetMode(TargetMode.valueOf(n.toUpperCase())), SerializeUtils.STRING, SharedKeys.TARGET_MODE), Side.SHARED);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(() -> getLockMode().name(), n -> setLockMode(LockMode.valueOf(n.toUpperCase())), SerializeUtils.STRING, SharedKeys.LOCK_MODE), Side.SHARED);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(() -> reverseCreateInput, b -> reverseCreateInput = b, SerializeUtils.BOOLEAN, REVERSE_CREATE_INPUT), Side.SHARED);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::isLocked, bl -> isLocked = bl, SerializeUtils.BOOLEAN, SharedKeys.IS_LOCKED), Side.SHARED);

        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> ITerminalDevice.super.deserialize(tag.getCompound("fields")),
                        tag -> tag.put("fields", ITerminalDevice.super.serialize()),
                        FIELD),
                Side.SHARED
        );
        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> getController().deserialize(tag.getCompound("controller")),
                        tag -> tag.put("controller", getController().serialize()),
                        CONTROLLER),
                Side.SHARED
        );
        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> getController().setTarget(tag.getDouble("controller_target")),
                        tag -> tag.putDouble("controller_target", getController().getTarget()),
                        SharedKeys.TARGET),
                Side.SHARED
        );
        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> getController().overrideError(tag.getDouble("controller_value")),
                        tag -> tag.putDouble("controller_value", getController().getValue()),
                        SharedKeys.VALUE),
                Side.SHARED
        );
        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        $ -> {if(targetMode == TargetMode.VELOCITY)getController().setTarget(0);},
                        $ -> {},
                        SharedKeys.TARGET),
                Side.SERVER_ONLY
        );
        * */


    }




    @Override
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
