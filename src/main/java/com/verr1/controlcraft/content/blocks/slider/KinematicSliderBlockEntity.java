package com.verr1.controlcraft.content.blocks.slider;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.controlcraft.content.blocks.SharedKeys;
import com.verr1.controlcraft.foundation.api.IKinematicUIDevice;
import com.verr1.controlcraft.foundation.type.Side;
import com.verr1.controlcraft.content.gui.legacy.ConstraintSliderScreen;
import com.verr1.controlcraft.foundation.api.IPacketHandler;
import com.verr1.controlcraft.foundation.api.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.control.KinematicController;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.*;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.descriptive.TargetMode;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;

import java.lang.Math;
import java.util.List;

public class KinematicSliderBlockEntity extends AbstractSlider implements
        ITerminalDevice, IPacketHandler, IKinematicUIDevice
{

    protected KinematicController controller = new KinematicController();

    protected double compliance = 1e-4;

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
        this.compliance = Math.max(compliance, 1e-5);
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
        if(Math.abs(targetOfLastAppliedConstraint - controller.getTarget()) < compliance)return;
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

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == RegisteredPacketType.SETTING_1){
            getController().setControlTarget(packet.getDoubles().get(0));
            setLerpSpeed(packet.getDoubles().get(1));
            setCompliance(packet.getDoubles().get(2));
        }
        if(packet.getType() == RegisteredPacketType.TOGGLE_0){
            setTargetMode(getTargetMode() == TargetMode.VELOCITY ? TargetMode.POSITION : TargetMode.VELOCITY);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == RegisteredPacketType.SYNC_0){
            double angle = packet.getDoubles().get(0);
            clientDistance = (float) angle;
        }
        if(packet.getType() == RegisteredPacketType.OPEN_SCREEN_0){
            double t = packet.getDoubles().get(0);
            double v = packet.getDoubles().get(1);
            double l = packet.getDoubles().get(2);
            double c = packet.getDoubles().get(3);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ScreenOpener.open(new ConstraintSliderScreen(getBlockPos(), v, t, c, l)));
        }
    }

}
