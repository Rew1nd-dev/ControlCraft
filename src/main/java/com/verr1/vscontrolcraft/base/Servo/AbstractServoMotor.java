package com.verr1.vscontrolcraft.base.Servo;


import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.Constrain.ConstrainCenter;
import com.verr1.vscontrolcraft.base.Constrain.DataStructure.ConstrainKey;
import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.base.DataStructure.SynchronizedField;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.base.DeferralExecutor.QueryConditionRunnable;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IConstrainHolder;
import com.verr1.vscontrolcraft.base.ShipConnectorBlockEntity;
import com.verr1.vscontrolcraft.base.UltraTerminal.*;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.ServoMotorPeripheral;
import com.verr1.vscontrolcraft.compat.valkyrienskies.servo.LogicalServoMotor;
import com.verr1.vscontrolcraft.compat.valkyrienskies.servo.ServoMotorForceInducer;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.constraints.*;
import org.valkyrienskies.core.impl.game.ships.ShipDataCommon;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.core.util.datastructures.DenseBlockPosSet;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.assembly.ShipAssemblyKt;

import javax.annotation.Nullable;
import java.lang.Math;
import java.util.List;
import java.util.Objects;

import static net.minecraft.ChatFormatting.GRAY;

public abstract class AbstractServoMotor extends ShipConnectorBlockEntity implements
        IHaveGoggleInformation, IConstrainHolder,
        IPIDController, ICanBruteDirectionalConnect,
        ITerminalDevice, IPacketHandler
{
    protected ServoMotorPeripheral peripheral;
    protected LazyOptional<IPeripheral> peripheralCap;

    private final LerpedFloat animatedLerpedAngle = LerpedFloat.angular();
    private float animatedAngle = 0;
    // the assembled face direction facing servo motor of this ship

    public SynchronizedField<ShipPhysics> ownPhysics = new SynchronizedField<>(ShipPhysics.EMPTY);
    public SynchronizedField<ShipPhysics> asmPhysics = new SynchronizedField<>(ShipPhysics.EMPTY);
    public SynchronizedField<Double> controlTorque = new SynchronizedField<>(0.0);

    private Vector3dc cached_OwnLocPos = new Vector3d();
    private Vector3dc cached_CmpLocPos = new Vector3d();


    private boolean isAdjustingAngle = false;

    private double offset = 0;

    private boolean softLockMode = false;

    private final PID defaultAngularModeParams = new PID(24, 0, 14);
    private final PID defaultAngularSpeedModeParams = new PID(10, 0, 0);

    private final PIDControllerInfoHolder servoController = new PIDControllerInfoHolder().setParameter(defaultAngularSpeedModeParams);


    public boolean isLocked() {
        return isLocked;
    }

    private boolean isLocked = false;



    private boolean cheatMode = false;

    public Vector3dc getCached_CmpLocPos() {return cached_CmpLocPos;}

    public void setCached_CmpLocPos(Vector3dc cached_CmpLocPos) {this.cached_CmpLocPos = new Vector3d(cached_CmpLocPos);}

    public Vector3dc getCached_OwnLocPos() {return cached_OwnLocPos;}

    public void setCached_OwnLocPos(Vector3dc cached_OwnLocPos) {this.cached_OwnLocPos = new Vector3d(cached_OwnLocPos);}

    public void updateCache(){
        VSConstraint savedFix = ConstrainCenter.get(new ConstrainKey(getBlockPos(), getDimensionID(), "attach_1", !isOnServerShip(), false, false));
        if(savedFix == null || savedFix.getConstraintType() != VSConstraintType.ATTACHMENT)return;
        VSAttachmentConstraint fix = (VSAttachmentConstraint) savedFix;
        setCached_CmpLocPos(fix.getLocalPos1());
        setCached_OwnLocPos(fix.getLocalPos0());
    }

    public void setOffset(double offset) {
        this.offset = offset;
        setChanged();
    }

    public double getOffset() {
        return offset;
    }



    public boolean isSoftLockMode() {
        return softLockMode;
    }

    public void setSoftLockMode(boolean softLockMode) {
        this.softLockMode = softLockMode;
        if(softLockMode && Math.abs(speed) < 1e-3)tryLock();
        else tryUnlock();
        setChanged();
    }

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    controlTorque::read,
                    controlTorque::write,
                    "Torque",
                    WidgetType.SLIDE,
                    ExposedFieldType.TORQUE
            ).withSuggestedRange(0, 1000),
            new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getTarget(),
                    t -> this.getControllerInfoHolder().setTarget(isAdjustingAngle ? VSMathUtils.clamp(t, Math.PI) : t),
                    "target",
                    WidgetType.SLIDE,
                    ExposedFieldType.TARGET
            ).withSuggestedRange(0, Math.PI / 2),
            new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getTarget(),
                    t -> this.getControllerInfoHolder().setTarget(isAdjustingAngle ? VSMathUtils.clamp(t, Math.PI) : t),
                    "target",
                    WidgetType.SLIDE,
                    ExposedFieldType.TARGET$1
            ).withSuggestedRange(0, Math.PI / 2),
            new ExposedFieldWrapper(
                    () -> (isLocked ? 1.0 : 0.0),
                    (d) -> {
                        if(d > (double) 1 / 15)tryLock();
                        else if(d < (double) 1 / 15)tryUnlock();
                    },
                    "Locked",
                    WidgetType.SLIDE,
                    ExposedFieldType.IS_LOCKED$1
            ),
            new ExposedFieldWrapper(
                    () -> (isLocked ? 1.0 : 0.0),
                    (d) -> {
                        if(d > (double) 1 / 15)tryLock();
                        else if(d < (double) 1 / 15)tryUnlock();
                    },
                    "Locked",
                    WidgetType.SLIDE,
                    ExposedFieldType.IS_LOCKED$2
            )
    );

    /*
    * new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getPIDParams().p(),
                    p -> this.getControllerInfoHolder().setP(p),
                    "P",
                    WidgetType.SLIDE,
                    ExposedFieldType.P
            ).withSuggestedRange(2, 36),
            new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getPIDParams().i(),
                    i -> this.getControllerInfoHolder().setI(i),
                    "I",
                    WidgetType.SLIDE,
                    ExposedFieldType.I
            ).withSuggestedRange(0, 2),
            new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getPIDParams().d(),
                    d -> this.getControllerInfoHolder().setD(d),
                    "D",
                    WidgetType.SLIDE,
                    ExposedFieldType.D
            ).withSuggestedRange(0, 18),
    *
    * */

    private ExposedFieldWrapper exposedField = fields.get(0); // isLocked

    private boolean reverseCreateInput = false;

    public boolean isReverseCreateInput() {
        return reverseCreateInput;
    }

    public void setReverseCreateInput(boolean reversed){
        reverseCreateInput = reversed;
        updateTargetFromCreate();
        setChanged();
    }

    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    public String name() {
        return "servo";
    }

    public boolean isAdjustingAngle() {
        return isAdjustingAngle;
    }

    public boolean isCheatMode() {
        return cheatMode;
    }

    public void setCheatMode(boolean cheatMode) {
        this.cheatMode = cheatMode;
        setChanged();
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        updateTargetFromCreate();
        softLockCheck();
    }

    public void softLockCheck(){
        if(isSoftLockMode() && Math.abs(speed) < 1e-3)tryLock();
        else if(isSoftLockMode() && !isAdjustingAngle() && Math.abs(getControllerInfoHolder().getTarget()) < 1e-3)tryLock();
        else tryUnlock();
    }

    public void updateTargetFromCreate(){
        double createInput2Omega = speed / 60 * 2 * Math.PI;
        double sign = reverseCreateInput ? -1 : 1;
        if(!isAdjustingAngle()) {
            getControllerInfoHolder().setTarget(createInput2Omega * sign);
        }else{
            double currentTarget = getControllerInfoHolder().getTarget();
            double newTarget = Util.radianReset(currentTarget + createInput2Omega * 0.05 * sign);
            getControllerInfoHolder().setTarget(newTarget);
        }
    }

    @Override
    public PIDControllerInfoHolder getControllerInfoHolder(){
        return servoController;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new ServoMotorPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public Direction getAlign() {
        return getDirection();
    }

    @Override
    public ExposedFieldWrapper getExposedField() {
        return exposedField;
    }

    @Override
    public Direction getForward() {
        return getServoDirection().getOpposite();
    }

    public AbstractServoMotor(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /*
    * @returns:  indicates the positive rotational direction vector in ship-coordinate
    * */
    public abstract Direction getServoDirection();

    public abstract Vector3d getServoDirectionJOML();

    public abstract BlockPos getAssembleBlockPos();

    public abstract Vector3d getAssembleBlockPosJOML(); // when shipified, where should the assembled block stay? this could be double vec3

    public Quaterniondc getQuaternionOfPlacement(){
        return VSMathUtils.getQuaternionOfPlacement(getServoDirection());
    }


    public double getServoAngle(){
        if(!hasCompanionShip())return 0;
        Matrix3dc own = ownPhysics.read().rotationMatrix().transpose(new Matrix3d()); //wc2sc
        Matrix3dc asm = asmPhysics.read().rotationMatrix().transpose(new Matrix3d());
        if(getCompanionShipDirection() == null)return 0;
        return VSMathUtils.get_yc2xc(own, asm, getServoDirection(), getCompanionShipDirection());
    }

    public double getServoAngularSpeed(){
        if(!hasCompanionShip())return 0;
        Matrix3dc own = ownPhysics.read().rotationMatrix().transpose(new Matrix3d()); //wc2sc
        Matrix3dc asm = asmPhysics.read().rotationMatrix().transpose(new Matrix3d());
        Vector3dc w_own = ownPhysics.read().omega();
        Vector3dc w_asm = asmPhysics.read().omega();
        if(getCompanionShipDirection() == null)return 0;
        return VSMathUtils.get_dyc2xc(own, w_own, w_asm,  getServoDirection(), getCompanionShipDirection());
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
        destroyConstrain();

    }


    public void setMode(boolean adjustAngle){
        isAdjustingAngle = adjustAngle;
        if(isAdjustingAngle){
            getControllerInfoHolder().setParameter(defaultAngularModeParams);
        }else{
            getControllerInfoHolder().setParameter(defaultAngularSpeedModeParams);
        }
        setChanged();
    }

    public void toggleMode(){
        setMode(!isAdjustingAngle);
    }


    public void syncCompanionAttachInducer(){
        if(level.isClientSide)return;
        ServerShip ship = getCompanionServerShip();
        if(ship == null)return;
        var inducer = ServoMotorForceInducer.getOrCreate(ship);
        inducer.update(new LevelPos(getBlockPos(), (ServerLevel) level));
    }

    public LogicalServoMotor getLogicalServoMotor(){
        if(level.isClientSide)return null;
        ServerShip ship = getCompanionServerShip();
        if(ship == null)return null;
        return new LogicalServoMotor(
                getServerShipID(),
                getCompanionShipID(),
                (ServerLevel) level,
                getServoDirection(),
                getCompanionShipDirection(),
                isAdjustingAngle,
                !isCheatMode(),
                getOutputTorque()
        );
    }

    public void tryLock(){
        if(!isLocked)lock();
    }

    public void tryUnlock(){
        if(isLocked)unlock();
    }

    public void lock(){
        if(level.isClientSide)return;
        if(!hasCompanionShip())return;

        Quaterniondc hingeQuaternion_Own = new
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(getServoDirection()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();

        double dumbFix = VSMathUtils.getDumbFixOfLockMode(getServoDirection(), getCompanionShipDirection());

        Quaterniondc hingeQuaternion_Cmp = new Quaterniond()
                .rotateAxis(dumbFix - getServoAngle(), getCompanionShipDirectionJOML())  // dumbFix +  dumb fixing getServoDirectionJOML()
                .mul(VSMathUtils.getQuaternionOfPlacement(getCompanionShipDirection().getOpposite()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();

        /*
        VSFixedOrientationConstraint fixed_ = new VSFixedOrientationConstraint(
                        getServerShipID(),
                        getCompanionShipID(),
                        1.0E-10,
                        hingeQuaternion_Own,
                        hingeQuaternion_Cmp,
                        1.0E10
                );
        * */



        Vector3dc v_own = hingeQuaternion_Own.transform(new Vector3d(0, 1, 0));
        Vector3dc v_cmp = hingeQuaternion_Cmp.transform(new Vector3d(0, 1, 0));

        // In case the cache didn't reload correctly
        // updateCache();
        VSAttachmentConstraint fixed = new VSAttachmentConstraint(
                getServerShipID(),
                getCompanionShipID(),
                1.0E-10,
                getCached_OwnLocPos().add(v_own, new Vector3d()),
                getCached_CmpLocPos().add(v_cmp, new Vector3d()),
                1.0E10,
                0.0
        );

        /*
        // Sometime this cause a pulse, I tried to fix it like this, but it didn't work

        ConstrainCenter.removeConstrain(new ConstrainKey(getBlockPos(), getDimensionID(), "hinge", isOnServerShip(), false, false));
        DeferralExecutor.executeLater(
                ()-> ConstrainCenter.createOrReplaceNewConstrain(
                        new ConstrainKey(getBlockPos(), getDimensionID(), "fixed", !isOnServerShip(), false, false),
                        fixed,
                        VSGameUtilsKt.getShipObjectWorld((ServerLevel) level)
                ),
                1
        );
        * */
        ConstrainCenter.createOrReplaceNewConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "fixed", !isOnServerShip(), false, false),
                fixed,
                VSGameUtilsKt.getShipObjectWorld((ServerLevel) level)
        );
        isLocked = true;
        setChanged();

    }


    public void unlock(){
        if(level.isClientSide)return;
        /*
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "fixed", !isOnServerShip(), false, false));
        ConstrainCenter.retrieveConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "hinge", isOnServerShip(), false, true),
                VSGameUtilsKt.getShipObjectWorld((ServerLevel) level)
        );
        * */
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "fixed", !isOnServerShip(), false, false));
        isLocked = false;
        setChanged();
    }


    public void bruteDirectionalConnectWith(BlockPos assemPos, Direction assemDir){
        Direction servoDir = getServoDirection();
        Vector3dc ownDir = getServoDirectionJOML();
        Vector3dc asmDir = Util.Vec3itoVector3d(assemDir.getNormal());

        ServerShip assembledShip = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) level, assemPos);
        if(assembledShip == null)return;
        long ownerShipID = getServerShipID();
        long assemShipID = assembledShip.getId();
        Quaterniondc hingeQuaternion_Own = new
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(servoDir))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();

        Quaterniondc hingeQuaternion_Cmp = new
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(assemDir.getOpposite()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();


        VSHingeOrientationConstraint hingeConstraint = new VSHingeOrientationConstraint(
                assemShipID,
                ownerShipID,
                1.0E-10,
                hingeQuaternion_Cmp, //new Quaterniond(),//
                hingeQuaternion_Own, //new Quaterniond(),//
                1.0E10
        );

        Vector3dc asmPos_Own = getAssembleBlockPosJOML();
        Vector3dc asmPos_Asm = Util.Vec3toVector3d(assemPos.getCenter());

        VSAttachmentConstraint attachment_1 = new VSAttachmentConstraint(
                ownerShipID,
                assemShipID,
                1.0E-10,
                new Vector3d(asmPos_Own).fma(1 + offset, ownDir),
                new Vector3d(asmPos_Asm).fma(-1, asmDir), // This is the opposite with the case of assemble()
                1.0E10,
                0.0
        );

        VSAttachmentConstraint attachment_2 = new VSAttachmentConstraint(
                ownerShipID,
                assemShipID,
                1.0E-10,
                new Vector3d(asmPos_Own).fma(-1, ownDir),
                new Vector3d(asmPos_Asm).fma(1 + offset, asmDir), // This is the opposite with the case of assemble()
                1.0E10,
                0.0
        );

        recreateConstrains(hingeConstraint, attachment_1, attachment_2);
        setCompanionShipID(assemShipID);
        setCompanionShipDirection(assemDir);
        setStartingAngleOfCompanionShip();
        notifyUpdate();
    }

    public void recreateConstrains(VSHingeOrientationConstraint hinge, VSAttachmentConstraint attach_1, VSAttachmentConstraint attach_2){
        var shipWorldCore = (ShipObjectServerWorld)VSGameUtilsKt.getShipObjectWorld((ServerLevel) level);

        boolean isGrounded = !isOnServerShip();

        ConstrainCenter.createOrReplaceNewConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "hinge", isGrounded, false, false),
                hinge,
                shipWorldCore
        );

        ConstrainCenter.createOrReplaceNewConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "attach_1", isGrounded, false, false),
                attach_1,
                shipWorldCore
        );
        ConstrainCenter.createOrReplaceNewConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "attach_2", isGrounded, false, false),
                attach_2,
                shipWorldCore
        );

        setCached_OwnLocPos(attach_1.getLocalPos0());
        setCached_CmpLocPos(attach_1.getLocalPos1());

    }

    @Override
    public void destroyConstrain() {
        boolean isGrounded = !isOnServerShip();
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "hinge", isGrounded, false, false));
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "attach_1", isGrounded, false, false));
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "attach_2", isGrounded, false, false));
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "fixed", !isOnServerShip(), false, false));
        clearCompanionShipInfo();
    }



    @Override
    public void bruteDirectionalConnectWith(BlockPos assemPos, Direction align, Direction forward){
        // motor assembly Does Not require to reference forward direction, since it will rotate along align-axis,
        // it will be fine as long as the ship is placed correctly before being assembled
        bruteDirectionalConnectWith(assemPos, align);
    }

    public void assemble(){
        // Only Assemble 1 Block When Being Right-Clicked with wrench. You Should Build Your Ship Up On This Assembled Block, Or Else Use Linker Tool Instead
        if(level.isClientSide)return;
        ServerLevel serverLevel = (ServerLevel) level;
        DenseBlockPosSet collectedBlocks = new DenseBlockPosSet();
        BlockPos assembledShipCenter = getAssembleBlockPos();
        if(serverLevel.getBlockState(assembledShipCenter).isAir())return;
        collectedBlocks.add(assembledShipCenter.getX(), assembledShipCenter.getY(), assembledShipCenter.getZ());
        ServerShip assembledShip = ShipAssemblyKt.createNewShipWithBlocks(assembledShipCenter, collectedBlocks, serverLevel);
        long assembledShipID = assembledShip.getId();

        Quaterniondc ownerShipQuaternion = getSelfShipQuaternion();
        Vector3d direction = getServoDirectionJOML();


        Vector3d assembledShipPosCenterShipAtOwner = getAssembleBlockPosJOML();
        Vector3d assembledShipPosCenterWorld = new Vector3d(assembledShipPosCenterShipAtOwner);
        if(isOnServerShip()){
            assembledShipPosCenterWorld = Objects.requireNonNull(getServerShipOn()).getShipToWorld().transformPosition(assembledShipPosCenterWorld);
        }

        long ownerShipID = getServerShipID();

        ((ShipDataCommon)assembledShip)
                .setTransform(
                        new ShipTransformImpl(
                                assembledShipPosCenterWorld,
                                assembledShip
                                        .getInertiaData()
                                        .getCenterOfMassInShip(),
                                ownerShipQuaternion,
                                new Vector3d(1, 1, 1)
                        )
                );



        Quaterniondc hingeQuaternion = new
                Quaterniond(getQuaternionOfPlacement())
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();


        VSHingeOrientationConstraint hingeConstraint = new VSHingeOrientationConstraint(
                assembledShipID,
                ownerShipID,
                1.0E-10,
                hingeQuaternion,
                hingeQuaternion,
                1.0E10
        );

        Vector3dc asmPos_Own = getAssembleBlockPosJOML();
        Vector3dc asmPos_Asm = assembledShip.getInertiaData().getCenterOfMassInShip().add(new Vector3d(0.5, 0.5, 0.5), new Vector3d());

        VSAttachmentConstraint attachment_1 = new VSAttachmentConstraint(
                ownerShipID,
                assembledShipID,
                1.0E-10,
                new Vector3d(asmPos_Own).fma(1 + offset, direction),
                new Vector3d(asmPos_Asm).fma(1, direction),
                1.0E10,
                0.0
        );

        VSAttachmentConstraint attachment_2 = new VSAttachmentConstraint(
                ownerShipID,
                assembledShipID,
                1.0E-10,
                new Vector3d(asmPos_Own).fma(-1, direction),
                new Vector3d(asmPos_Asm).fma(-(1 + offset), direction),
                1.0E10,
                0.0
        );



        recreateConstrains(hingeConstraint, attachment_1, attachment_2);
        setCompanionShipID(assembledShipID);
        setCompanionShipDirection(getServoDirection().getOpposite());
        notifyUpdate();

    }

    public void constrainAlive(){
        boolean isGrounded = !isOnServerShip();
        ConstrainCenter.alive(new ConstrainKey(getBlockPos(), getDimensionID(), "hinge", isGrounded, false, false));
        ConstrainCenter.alive(new ConstrainKey(getBlockPos(), getDimensionID(), "attach_1", isGrounded, false, false));
        ConstrainCenter.alive(new ConstrainKey(getBlockPos(), getDimensionID(), "attach_2", isGrounded, false, false));
    }

    @Override
    public void tick() {
        super.tick();
        if(isAdjustingAngle())updateTargetFromCreate();
        // softLockCheck();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if(level.isClientSide)return;
        constrainAlive();
        syncClient(getBlockPos(), level);
    }

    public void setStartingAngleOfCompanionShip(){
        ServerShip asm = getCompanionServerShip();
        ServerShip own = getServerShipOn();
        if(asm == null)return;
        if(!isAdjustingAngle())return;
        double startAngle = VSMathUtils.get_yc2xc(own, asm, getServoDirection(), getCompanionShipDirection());
        getControllerInfoHolder().setTarget(startAngle);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        var c =  Components.translatable(ControlCraft.MODID + ".title.tooltip.motor").withStyle(GRAY).getString();

        tooltip.add(Component.literal("    " + c));

        float angle = (float) Math.toDegrees(animatedAngle);

        Lang.number(angle)
                .text("°")
                .style(ChatFormatting.AQUA)
                .space()
                .forGoggles(tooltip, 1);

        Direction dir = WandRenderer.lookingAtFaceDirection();
        if(dir == null)return true;
        tooltip.add(Components.literal("Face " + dir + " Bounded:"));
        fields().forEach(f -> {
            if(!f.directionOptional.test(dir))return;
            String info = f.type.getComponent().getString();
            tooltip.add(Component.literal(info).withStyle(ChatFormatting.AQUA));
        });

        return true;
    }

    public void syncClient(){
        if(!level.isClientSide){
            var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.SYNC_0)
                    .withDouble(getServoAngle())
                    .build();
            AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    public void tickAnimation(){
        animatedLerpedAngle.chase(Math.toDegrees(animatedAngle), 0.5, LerpedFloat.Chaser.EXP);
        animatedLerpedAngle.tickChaser();
    }

    public void setAnimatedAngle(double angle) {
        animatedAngle = (float)angle;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SYNC_0){
            double angle = packet.getDoubles().get(0);
            setAnimatedAngle(angle);
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SETTING_0){
            double offset = packet.getDoubles().get(0);
            setOffset(offset);
        }
        if(packet.getType() == BlockBoundPacketType.TOGGLE_0){
            setCheatMode(!isCheatMode());
        }
        if(packet.getType() == BlockBoundPacketType.TOGGLE_1){
            setReverseCreateInput(!isReverseCreateInput());
        }
        if(packet.getType() == BlockBoundPacketType.TOGGLE_2){
            setSoftLockMode(!isSoftLockMode());
        }
    }

    public float getAnimatedAngle(float partialTick) {
        return animatedLerpedAngle.getValue(partialTick);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if(clientPacket)return;
        fields.forEach(e -> tag.put("field_" + e.type.name(), e.serialize()));
        tag.putInt("exposedField", fields.indexOf(exposedField));
        tag.put("controller", getControllerInfoHolder().serialize());
        tag.putBoolean("cheatMode", isCheatMode());
        tag.putBoolean("isLocked", isLocked);
        tag.putBoolean("angleMode", isAdjustingAngle);
        tag.putDouble("offset", offset);
        tag.putBoolean("reverseCreate", reverseCreateInput);
        tag.putBoolean("softLockMode", softLockMode);
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
        getControllerInfoHolder().deserialize(tag.getCompound("controller"));
        cheatMode = tag.getBoolean("cheatMode");
        isLocked = tag.getBoolean("isLocked");
        isAdjustingAngle = tag.getBoolean("angleMode");
        offset = tag.getDouble("offset");
        reverseCreateInput = tag.getBoolean("reverseCreate");
        softLockMode = tag.getBoolean("softLockMode");

        if(!isAdjustingAngle){
            getControllerInfoHolder().setTarget(0);
        }


        DeferralExecutor.executeLater(new UpdateCacheWhenHasLevel());
    }

    // When The Level Try to Reload This BlockEntity, The Level Might Be Null, And getDimensionID() Won't Return The Correct Value
    private class UpdateCacheWhenHasLevel implements QueryConditionRunnable{

        @Override
        public boolean condition() {
            return hasLevel();
        }

        @Override
        public void run() {
            updateCache();
        }
    }

}



