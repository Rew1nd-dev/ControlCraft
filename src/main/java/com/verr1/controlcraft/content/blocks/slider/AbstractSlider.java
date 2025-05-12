package com.verr1.controlcraft.content.blocks.slider;

import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.controlcraft.config.BlockPropertyConfig;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.content.blocks.SharedKeys;
import com.verr1.controlcraft.content.blocks.ShipConnectorBlockEntity;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.network.executors.ClientBuffer;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.foundation.api.operatable.IBruteConnectable;
import com.verr1.controlcraft.foundation.api.operatable.IConstraintHolder;
import com.verr1.controlcraft.foundation.data.ShipPhysics;
import com.verr1.controlcraft.foundation.data.constraint.ConnectContext;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.foundation.vsapi.ShipAssembler;
import com.verr1.controlcraft.foundation.vsapi.VSJointPose;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.SerializeUtils;
import com.verr1.controlcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.constraints.VSConstraint;
import org.valkyrienskies.core.apigame.constraints.VSFixedOrientationConstraint;
import org.valkyrienskies.core.apigame.constraints.VSSlideConstraint;
import org.valkyrienskies.core.impl.game.ships.ShipDataCommon;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;

import java.util.List;
import java.util.Optional;

import static com.verr1.controlcraft.content.blocks.SharedKeys.*;
import static com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies.toJOML;

public abstract class AbstractSlider extends ShipConnectorBlockEntity implements
        IConstraintHolder, IBruteConnectable
{
    public static NetworkKey ANIMATED_DISTANCE = NetworkKey.create("animated_distance");

    protected double MAX_SLIDE_DISTANCE = BlockPropertyConfig._PHYSICS_MAX_SLIDE_DISTANCE;

    protected ConnectContext context = ConnectContext.EMPTY;

    private final LerpedFloat clientLerpedDistance = LerpedFloat.linear();

    private Vector3d selfOffset = new Vector3d();



    private Vector3d compOffset = new Vector3d();

    public float clientDistance = 0;

    public AbstractSlider(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::getSlideDistance, this::setClientDistance, SerializeUtils.DOUBLE, ANIMATED_DISTANCE), Side.RUNTIME_SHARED);
        // registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::getOffset, this::setOffset, SerializeUtils.VECTOR3D, AbstractMotor.OFFSET), Side.SHARED);

        buildRegistry(ANIMATED_DISTANCE).withBasic(SerializePort.of(this::getSlideDistance, this::setClientDistance, SerializeUtils.DOUBLE)).dispatchToSync().runtimeOnly().register();
        buildRegistry(SELF_OFFSET).withBasic(SerializePort.of(() -> new Vector3d(getSelfOffset()), this::setSelfOffset, SerializeUtils.VECTOR3D)).withClient(ClientBuffer.VECTOR3D.get()).register();
        buildRegistry(COMP_OFFSET).withBasic(SerializePort.of(() -> new Vector3d(getCompOffset()), this::setCompOffset, SerializeUtils.VECTOR3D)).withClient(ClientBuffer.VECTOR3D.get()).register();
        buildRegistry(CONNECT_CONTEXT).withBasic(SerializePort.of(() -> context, ctx -> context = ctx, SerializeUtils.CONNECT_CONTEXT)).register();


        panel().registerUnit(SharedKeys.ASSEMBLE, this::assemble);
        panel().registerUnit(SharedKeys.DISASSEMBLE, this::destroyConstraints);

        registerConstraintKey("slide");
        registerConstraintKey("orient");
    }

    @Override
    public void tickClient() {
        super.tickClient();
        tickAnimation();
    }

    public Vector3d getCompOffset() {
        return compOffset;
    }

    public void setCompOffset(Vector3dc compOffset) {
        this.compOffset = new Vector3d(compOffset);
    }

    public void setClientDistance(double clientDistance) {
        this.clientDistance = (float) clientDistance;
    }

    @Override
    public void tickServer() {
        super.tickServer();
        syncForNear(true, ANIMATED_DISTANCE);
        // syncClient();
    }



    public Vector3dc getSelfOffset() {
        return new Vector3d();
    }


    public void setSelfOffset(Vector3dc selfOffset) {

    }

    public void syncClient(){
        if(level == null || level.isClientSide)return;
        var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.SYNC_0)
                .withDouble(getSlideDistance())
                .build();
        ControlCraftPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
    }

    public void tickAnimation(){
        clientLerpedDistance.chase(clientDistance, 0.5, LerpedFloat.Chaser.EXP);
        clientLerpedDistance.tickChaser();
    }

    public void setAnimatedDistance(float d){
        clientDistance = (float) VSMathUtils.clamp0(d, MAX_SLIDE_DISTANCE);

    }

    public double getAnimatedTargetDistance(float partialTicks) {
        return clientLerpedDistance.getValue(partialTicks);
    }


    @Override
    public void destroyConstraints() {
        clearCompanionShipInfo();
        removeConstraint("slide");
        removeConstraint("orient");
    }

    @Override
    public void bruteDirectionalConnectWith(BlockPos bp_comp, Direction align_comp, Direction forward_comp) {
        if(!VSMathUtils.isVertical(align_comp, forward_comp))return;
        Direction align_self = getAlign();
        Direction forward_self = getForward();
        Ship compShip = ValkyrienSkies.getShipManagingBlock(level, bp_comp);
        if(compShip == null)return;
        long selfId = getShipOrGroundID();
        long compId = compShip.getId();

        float m = (float)(MAX_SLIDE_DISTANCE);

        Quaterniondc q_self = VSMathUtils.getQuaternionOfPlacement(align_self, forward_self).conjugate();
        Quaterniondc q_comp = VSMathUtils.getQuaternionOfPlacement(align_comp.getOpposite(), forward_comp).conjugate();


        Vector3dc p_self = getAssembleBlockPosJOML();
        Vector3dc p_comp = ValkyrienSkies.set(new Vector3d(), bp_comp.getCenter());

        /*
        VSPrismaticJoint joint = new VSPrismaticJoint(
                selfId,
                new VSJointPose(p_self, q_self),
                compId,
                new VSJointPose(p_comp, q_comp),
                new VSJointMaxForceTorque(1e20f, 1e20f),
                new VSD6Joint.LinearLimitPair(-m, m, null, null, null, null)
        );
        * */



        VSFixedOrientationConstraint orientation = new VSFixedOrientationConstraint(
                selfId,
                compId,
                1.0E-20,
                q_self,
                q_comp,
                1.0E20
        );

        VSSlideConstraint slide = new VSSlideConstraint(
                selfId,
                compId,
                1.0E-20,
                p_self,
                p_comp,
                1.0E20,
                getDirectionJOML(),
                MAX_SLIDE_DISTANCE
        );

        recreateConstrains(orientation, slide);
        setCompanionShipID(compId);
        setCompanionShipDirection(align_comp);
        setChanged();

    }

    public void recreateConstrains(VSConstraint... joint) {
        if(level == null || level.isClientSide)return;
        if(joint.length < 2){
            ControlCraft.LOGGER.error("invalid constraint data for slider");
            return;
        }
        overrideConstraint("orient", joint[0]);
        overrideConstraint("slide", joint[1]);
        updateConnectContext();
    }

    public void invalidateConnectContext(){
        context = ConnectContext.EMPTY;
    }

    public void updateConnectContext(){
        Optional.ofNullable(getConstraint("slide")).ifPresentOrElse(
                sld -> Optional.ofNullable(getConstraint("orient")).ifPresentOrElse(
                fx -> {
                    VSSlideConstraint slide = (VSSlideConstraint) sld;
                    VSFixedOrientationConstraint fix = (VSFixedOrientationConstraint) fx;
                    Vector3dc p0 = slide.getLocalPos0();
                    Vector3dc p1 = slide.getLocalPos1();
                    Quaterniondc q0 = fix.getLocalRot0();
                    Quaterniondc q1 = fix.getLocalRot1();

                    context = new ConnectContext(
                            new VSJointPose(p0, q0),
                            new VSJointPose(p1, q1),
                            false
                    );
                },
                    this::invalidateConnectContext
                ),
                    this::invalidateConnectContext
                );
    }

    public double getSlideDistance(){
        if(noCompanionShip())return 0;
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

        Direction dir = getAlign(); // getDirection
        double sign = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;

        return switch (dir.getAxis()){
            case X -> sign * sub_sc.x();
            case Y -> sign * sub_sc.y();
            case Z -> sign * sub_sc.z();
        };
    }

    public void assemble(){
        // Only Assemble 1 Block When Being Right-Clicked with wrench. You Should Build Your Ship Up On This Assembled Block, Or Else Use Linker Tool Instead
        if(level == null || level.isClientSide)return;
        Ship self = getShipOn();
        // if(self == null)return;
        ServerLevel serverLevel = (ServerLevel) level;
        List<BlockPos> collected = List.of(getAssembleBlockPos());
        ServerShip comp = ShipAssembler.INSTANCE.assembleToShip(serverLevel, collected.get(0), true, 1, true);

        Vector3dc comp_at_sc = toJOML(getAssembleBlockPos().getCenter());
        Vector3dc comp_at_wc = getShipOn() != null ?
                getShipOn().getShipToWorld().transformPosition(comp_at_sc, new Vector3d()) :
                new Vector3d(comp_at_sc);
        ((ShipDataCommon)comp).setTransform(
                new ShipTransformImpl(
                        comp_at_wc,
                        comp.getInertiaData().getCenterOfMassInShip(),
                        getSelfShipQuaternion(),
                        new Vector3d(1, 1, 1)
                ));

        long compId = comp.getId();
        long selfId = getShipOrGroundID();
        Vector3dc selfContact = getAssembleBlockPosJOML();
        Vector3dc compContact = comp.getInertiaData().getCenterOfMassInShip().add(new Vector3d(0.5, 0.5, 0.5), new Vector3d());
        // Vector3dc selfOffset = self.getTransform().getPositionInShip();  //.sub(selfContact, new Vector3d())

        // float m = (float)(MAX_SLIDE_DISTANCE);

        Quaterniondc selfQuaternion = new Quaterniond(); // VSMathUtils.getQuaternionToEast_(getDirection());
        Quaterniondc compQuaternion = new Quaterniond(); // VSMathUtils.getQuaternionToEast_(getDirection());
        /*
        VSPrismaticJoint joint = new VSPrismaticJoint(
                selfId,
                new VSJointPose(selfContact, selfQuaternion),
                compId,
                new VSJointPose(compContact, compQuaternion),
                new VSJointMaxForceTorque(1e20f, 1e20f),
                new VSD6Joint.LinearLimitPair(-(float) MAX_SLIDE_DISTANCE, (float) MAX_SLIDE_DISTANCE, null, null, null, null)
        );
        * */


        VSFixedOrientationConstraint orientation = new VSFixedOrientationConstraint(
                selfId,
                compId,
                1.0E-20,
                selfQuaternion,
                compQuaternion,
                1.0E20
        );

        VSSlideConstraint slide = new VSSlideConstraint(
                selfId,
                compId,
                1.0E-20,
                selfContact,
                compContact,
                1.0E20,
                getDirectionJOML(),
                MAX_SLIDE_DISTANCE
        );

        recreateConstrains(orientation, slide);
        setCompanionShipID(compId);
        setCompanionShipDirection(getDirection().getOpposite());
        setChanged();

    }

    public abstract @NotNull Direction getSlideDirection();

    @Override
    public Direction getAlign() {
        return getSlideDirection();
    }

    @Override
    public Direction getForward() {
        return getVertical().getOpposite();
    }

    public Direction getVertical(){
        if(getAlign().getAxis() != Direction.Axis.Y)return Direction.UP;
        return Direction.SOUTH;
    }


    public BlockPos getAssembleBlockPos(){
        return getBlockPos().relative(getDirection());
    }

    public Vector3d getAssembleBlockPosJOML(){
        return ValkyrienSkies.set(new Vector3d(), getAssembleBlockPos().getCenter());
    }




}
