package com.verr1.controlcraft.content.blocks.motor;

import com.verr1.controlcraft.content.blocks.SharedKeys;
import com.verr1.controlcraft.content.valkyrienskies.attachments.KinematicMotorForceInducer;
import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.content.valkyrienskies.transform.KinematicMotorTransformProvider;
import com.verr1.controlcraft.content.valkyrienskies.transform.LerpedTransformProvider;
import com.verr1.controlcraft.foundation.api.IKinematicUIDevice;
import com.verr1.controlcraft.foundation.data.GroundBodyShip;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.type.Side;
import com.verr1.controlcraft.content.cctweaked.peripheral.KinematicMotorPeripheral;
import com.verr1.controlcraft.foundation.api.IPacketHandler;
import com.verr1.controlcraft.foundation.api.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.control.KinematicController;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.data.logical.LogicalKinematicMotor;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.descriptive.TargetMode;
import com.verr1.controlcraft.foundation.vsapi.PhysPose;
import com.verr1.controlcraft.utils.SerializeUtils;
import com.verr1.controlcraft.utils.VSMathUtils;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;

import java.lang.Math;
import java.util.List;
import java.util.Optional;

public abstract class AbstractKinematicMotor extends AbstractMotor implements
        ITerminalDevice, IPacketHandler, IKinematicUIDevice
{
    protected KinematicController controller = new KinematicController();

    protected double compliance = 1e-4;

    protected TargetMode mode = TargetMode.VELOCITY;

    protected boolean USE_CONSTRAINT_SPAMMING = true;

    protected double targetOfLastAppliedConstraint = 114514; // magic number : )

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    () -> controller.getControlTarget(),
                    t -> controller.setControlTarget(t),
                    "target",
                    ExposedFieldType.FORCED_TARGET
            ).withSuggestedRange(0, Math.PI / 2),
            new ExposedFieldWrapper(
                    () -> controller.getControlTarget(),
                    t -> controller.setControlTarget(t),
                    "target",
                    ExposedFieldType.FORCED_TARGET$1
            ).withSuggestedRange(0, Math.PI / 2)
    );

    private KinematicMotorPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new KinematicMotorPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }


    public void setCompliance(double compliance) {
        this.compliance = compliance;
        setChanged();
    }

    public double getCompliance() {
        return compliance;
    }

    public TargetMode getTargetMode() {
        return mode;
    }

    public void setTargetMode(TargetMode mode) {
        this.mode = mode;
        setChanged();
    }

    public KinematicController getController() {
        return controller;
    }

    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    public String name() {
        return "constraint_motor";
    }

    public void setMode(boolean isAdjustingAngle) {
        this.mode = isAdjustingAngle ? TargetMode.POSITION : TargetMode.VELOCITY;
        setChanged();
    }

    public AbstractKinematicMotor(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        registerConstraintKey("control");
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(() -> compliance, c -> compliance = c, SerializeUtils.DOUBLE, SharedKeys.COMPLIANCE), Side.SHARED);
        registerFieldReadWriter(
                SerializeUtils.ReadWriter.of(
                        () -> getTargetMode().name(),
                        n -> setTargetMode(TargetMode.valueOf(n.toUpperCase())),
                        SerializeUtils.STRING,
                        SharedKeys.TARGET_MODE),
                Side.SHARED);
        registerFieldReadWriter(
                SerializeUtils.ReadWriter.of(
                        () -> context,
                        ctx -> context = ctx,
                        SerializeUtils.CONNECT_CONTEXT,
                        SharedKeys.CONNECT_CONTEXT),
                Side.SERVER_ONLY);

        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> ITerminalDevice.super.deserialize(tag.getCompound("fields")),
                        tag -> tag.put("fields", ITerminalDevice.super.serialize()),
                        FIELD),
                Side.SHARED
        );
        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> getController().deserialize(tag.getCompound("controller_target")),
                        tag -> tag.put("controller_target", getController().serialize()),
                        SharedKeys.TARGET),
                Side.SHARED
        );
        // for kinematic device, target is actual value
        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        tag -> getController().setTarget(tag.getDouble("controller")),
                        tag -> tag.putDouble("controller", getController().getTarget()),
                        SharedKeys.VALUE),
                Side.SHARED
        );

        registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor.of(
                        $ -> {if(getTargetMode() == TargetMode.VELOCITY)getController().setControlTarget(0);},
                        $ -> {},
                        SharedKeys.PLACE_HOLDER),
                Side.SERVER_ONLY
        );
    }


    private void tickTarget(){
        if(mode == TargetMode.VELOCITY){
            controller.updateTargetAngular(0.05);
        }else{
            controller.updateForcedTarget();
        }
    }

    private void tickConstraint(){
        tickTarget();
        if(Math.abs(targetOfLastAppliedConstraint - controller.getTarget()) < compliance + 1e-4)return;
        if(level == null || level.isClientSide)return;
        long compID = Optional.ofNullable(getCompanionServerShip()).map(Ship::getId).orElse(-1L);
        if(compID == -1)return;
        Quaterniondc q_self = new
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(getServoDirection()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();


        double AngleFix = VSMathUtils.getDumbFixOfLockMode(getServoDirection(), getCompanionShipAlign());

        Quaterniondc q_comp = new Quaterniond()
                .rotateAxis(AngleFix - getController().getTarget(), getCompanionShipAlignJOML())  // dumbFix +  dumb fixing getServoDirectionJOML()
                .mul(VSMathUtils.getQuaternionOfPlacement(getCompanionShipAlign().getOpposite()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();

        Vector3dc v_own = q_self.transform(new Vector3d(0, 1, 0));
        Vector3dc v_cmp = q_comp.transform(new Vector3d(0, 1, 0));


        VSAttachmentConstraint fixed = new VSAttachmentConstraint(
                getShipOrGroundID(),
                getCompanionShipID(),
                1.0E-20,
                context.self().getPos().add(v_own, new Vector3d()),
                context.comp().getPos().add(v_cmp, new Vector3d()),
                1.0E20,
                0.0
        );
        overrideConstraint("control", fixed);
        targetOfLastAppliedConstraint = controller.getTarget();
    }

    @Override
    public void destroyConstraints() {
        Optional.ofNullable(getCompanionServerShip()).ifPresent(s -> s.setStatic(false));
        clearCompanionShipInfo();
        super.destroyConstraints(); // set non-static before ship info is cleared
        if(USE_CONSTRAINT_SPAMMING){
            destroyConstraintForMode();
        }
    }

    private void destroyConstraintForMode(){
        removeConstraint("revolute");
        removeConstraint("attach_1");
        removeConstraint("attach_2");
        removeConstraint("control");
    }

    @Override
    public void bruteDirectionalConnectWith(BlockPos bp_comp, Direction dir_comp, Direction forward) {
        super.bruteDirectionalConnectWith(bp_comp, dir_comp, forward);
        if(!USE_CONSTRAINT_SPAMMING){
            destroyConstraintForMode();
        }
    }

    @Override
    public void setStartingAngleOfCompanionShip() {
        Ship asm = getCompanionServerShip();
        Ship own = getShipOn();
        if(asm == null)return;
        double target = VSMathUtils.get_yc2xc(own, asm, getServoDirection(), getCompanionShipAlign());
        controller.setTarget(target);
    }

    @Override
    public void assemble() {
        super.assemble();
        if(!USE_CONSTRAINT_SPAMMING){
            destroyConstraintForMode();
        }
    }

    public @Nullable PhysPose tickPose(){
        LoadedServerShip compShip = getCompanionServerShip();
        LogicalKinematicMotor motor = getLogicalMotor();
        Ship selfShip = getShipOn();
        if(compShip == null || motor == null)return null;
        return InducerControls.kinematicMotorTickControls(
                motor,
                Optional.ofNullable(selfShip).orElse(new GroundBodyShip()),
                compShip
        );
    }

    @Override
    public void tickServer() {
        super.tickServer();
        if(USE_CONSTRAINT_SPAMMING) {
            tickConstraint();
        } else{
            syncAttachTransformProviderServer();
        }
    }

    public void syncAttachInducer(){
        if(level == null || level.isClientSide)return;
        Optional
                .ofNullable(getCompanionServerShip())
                .map(KinematicMotorForceInducer::getOrCreate)
                .ifPresent(inducer -> inducer.alive(WorldBlockPos.of(level, getBlockPos())));
    }

    // simply for debugging


    @Override
    public void tickClient() {
        super.tickClient();
        // syncAttachTransformProviderClient();
    }

    public void syncAttachTransformProviderClient(){
        if(level != null && !level.isClientSide)return;
        Optional
                .ofNullable(getCompanionClientShip())
                .ifPresent(LerpedTransformProvider::replaceOrCreate);
    }

    public void syncAttachTransformProviderServer(){
        if(level != null && level.isClientSide)return;
        Optional
                .ofNullable(getCompanionServerShip())
                .map(KinematicMotorTransformProvider::replaceOrCreate)
                .ifPresent(prov -> Optional.ofNullable(tickPose()).ifPresent(prov::set));
    }


    public @Nullable LogicalKinematicMotor getLogicalMotor() {
        if(level == null || level.isClientSide)return null;
        if(noCompanionShip() || context.isDirty())return null;
        return new LogicalKinematicMotor(
                getShipOrGroundID(),
                getCompanionShipID(),
                context,
                getTargetMode() == TargetMode.POSITION,
                getServoDirection(),
                getCompanionShipAlign(),
                controller
        );
    }
}
