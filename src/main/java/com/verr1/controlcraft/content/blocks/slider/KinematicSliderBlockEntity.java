package com.verr1.controlcraft.content.blocks.slider;

import com.verr1.controlcraft.content.blocks.SharedKeys;
import com.verr1.controlcraft.content.gui.layouts.api.IKinematicUIDevice;
import com.verr1.controlcraft.foundation.network.executors.ClientBuffer;
import com.verr1.controlcraft.foundation.network.executors.CompoundTagPort;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.foundation.api.IPacketHandler;
import com.verr1.controlcraft.foundation.api.delegate.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.control.KinematicController;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.type.*;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.descriptive.TargetMode;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;

import java.lang.Math;
import java.util.List;

import static com.verr1.controlcraft.content.blocks.SharedKeys.*;

public class KinematicSliderBlockEntity extends AbstractSlider implements
        ITerminalDevice, IPacketHandler, IKinematicUIDevice
{

    protected KinematicController controller = new KinematicController();

    protected double compliance = -5;

    protected TargetMode mode = TargetMode.VELOCITY;

    protected double targetOfLastAppliedConstraint = 114514; // magic number : )



    protected double lerpSpeed = 5;

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
    }

    public KinematicController getController() {
        return controller;
    }

    public double getLerpSpeed() {
        return lerpSpeed;
    }

    public void setLerpSpeed(double lerpSpeed) {
        this.lerpSpeed = lerpSpeed;
    }

    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    public String name() {
        return "constraint_slider";
    }

    public void setMode(boolean isAdjustingAngle) {
        this.mode = isAdjustingAngle ? TargetMode.POSITION : TargetMode.VELOCITY;
    }

    public KinematicSliderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        registerConstraintKey("control");
        buildRegistry(SharedKeys.COMPLIANCE).withBasic(SerializePort.of(this::getCompliance, this::setCompliance, SerializeUtils.DOUBLE)).withClient(ClientBuffer.DOUBLE.get()).register();
        buildRegistry(TARGET_MODE)
                .withBasic(SerializePort.of(this::getTargetMode, this::setTargetMode, SerializeUtils.ofEnum(TargetMode.class)))
                .withClient(ClientBuffer.of(TargetMode.class))
                .register();
        buildRegistry(CONNECT_CONTEXT).withBasic(SerializePort.of(() -> context, ctx -> context = ctx, SerializeUtils.CONNECT_CONTEXT)).register();

        buildRegistry(SharedKeys.TARGET).withBasic(SerializePort.of(() -> getController().getControlTarget(), t -> getController().setControlTarget(t), SerializeUtils.DOUBLE)).withClient(ClientBuffer.DOUBLE.get()).register();
        buildRegistry(SharedKeys.VALUE).withBasic(SerializePort.of(() -> getController().getTarget(), $ -> {}, SerializeUtils.DOUBLE)).withClient(ClientBuffer.DOUBLE.get()).register();
        buildRegistry(PLACE_HOLDER)
                .withBasic(CompoundTagPort.of(
                        CompoundTag::new,
                        $ ->  {if(getTargetMode() == TargetMode.VELOCITY)getController().setTarget(0);}
                ))
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
    }

    @Override
    public @NotNull Direction getSlideDirection() {
        return getDirection();
    }

    private void tickTarget(){
        if(mode == TargetMode.VELOCITY){
            controller.updateTargetLinear(0.05, 0, MAX_SLIDE_DISTANCE);
        }else{
            controller.updateForcedTarget();
            // controller.updateLerpedLinearTarget(lerpSpeed, 0.05);
        }
    }

    private void tickConstraint(){
        tickTarget();
        if(Math.abs(targetOfLastAppliedConstraint - controller.getTarget()) < Math.pow(10, compliance) + 1e-6)return;
        if(level == null || level.isClientSide)return;
        Ship compShip = getCompanionServerShip();
        if(compShip == null)return;
        long selfId = getShipOrGroundID();
        long compId = compShip.getId();

        Vector3dc sliDir = ValkyrienSkies.set(new Vector3d(), getSlideDirection().getNormal());
        /*
        VSJoint joint = new VSFixedJoint(
                selfId,
                new VSJointPose(context.self().getPos(), context.self().getRot()),
                compId,
                new VSJointPose(context.comp().getPos().fma(
                        -MathUtils.clamp(
                                controller.getTarget(),
                                0.0,
                                MAX_SLIDE_DISTANCE
                        ), slideDirJoml, new Vector3d()),
                        context.comp().getRot()
                ),
                new VSJointMaxForceTorque(1e20f, 1e20f)
        );
        overrideConstraint("control", joint);
        * */
        VSAttachmentConstraint fixed = new VSAttachmentConstraint(
                selfId,
                compId,
                1.0E-20,
                context.self().getPos().fma(getController().getTarget(), sliDir, new Vector3d()),
                context.comp().getPos(), // This is the opposite with the case of assemble()
                1.0E20,
                0.0
        );
        overrideConstraint("control", fixed);
        targetOfLastAppliedConstraint = controller.getTarget();
    }


    @Override
    public void destroyConstraints() {
        super.destroyConstraints();
        removeConstraint("control");
    }

    @Override
    public void bruteDirectionalConnectWith(BlockPos bp_comp, Direction align_comp, Direction forward_comp) {
        super.bruteDirectionalConnectWith(bp_comp, align_comp, forward_comp);
    }

    @Override
    public void assemble() {
        super.assemble();
    }

    @Override
    public void tickServer() {
        super.tickServer();
        syncForNear(true, FIELD);
        tickConstraint();
        // tickPose();
        // syncAttachInducer();
    }



    public void displayScreen(ServerPlayer player){

        double t = getController().getControlTarget();
        double v = getSlideDistance();
        double l = getLerpSpeed();
        double c = getCompliance();

        var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.OPEN_SCREEN_0)
                .withDouble(t)
                .withDouble(v)
                .withDouble(l)
                .withDouble(c)
                .build();

        ControlCraftPackets.sendToPlayer(p, player);
    }


}
