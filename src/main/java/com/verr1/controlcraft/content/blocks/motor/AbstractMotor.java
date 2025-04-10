package com.verr1.controlcraft.content.blocks.motor;

import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.content.blocks.ShipConnectorBlockEntity;
import com.verr1.controlcraft.content.compact.vmod.VSchematicCompactCenter;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.network.executors.ClientBuffer;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.foundation.api.IBruteConnectable;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;
import org.valkyrienskies.core.apigame.constraints.VSConstraint;
import org.valkyrienskies.core.apigame.constraints.VSHingeOrientationConstraint;
import org.valkyrienskies.core.impl.game.ships.ShipDataCommon;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;

import java.lang.Math;
import java.util.List;
import java.util.Optional;

import static com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies.toJOML;
import static com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies.toMinecraft;

public abstract class AbstractMotor extends ShipConnectorBlockEntity implements IBruteConnectable
{
    public static NetworkKey OFFSET = NetworkKey.create("offset");
    public static NetworkKey ANIMATED_ANGLE = NetworkKey.create("animated_angle");


    protected float clientAngle = 0;
    protected final LerpedFloat clientLerpedAngle = LerpedFloat.angular();

    public ConnectContext context = ConnectContext.EMPTY;
    protected Vector3d offset = new Vector3d();

    public AbstractMotor(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::getServoAngle, this::setClientAngle, SerializeUtils.DOUBLE, ANIMATED_ANGLE), Side.RUNTIME_SHARED);
        // registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::getOffset, this::setOffset, SerializeUtils.VECTOR3DC, OFFSET), Side.SHARED);

        buildRegistry(ANIMATED_ANGLE).withBasic(SerializePort.of(this::getServoAngle, this::setClientAngle, SerializeUtils.DOUBLE)).dispatchToSync().runtimeOnly().register();
        buildRegistry(OFFSET).withBasic(SerializePort.of(() -> new Vector3d(getOffset()), this::setOffset, SerializeUtils.VECTOR3D)).withClient(ClientBuffer.VECTOR3D.get()).register();

        registerConstraintKey("revolute");
        registerConstraintKey("attach_1");
        registerConstraintKey("attach_2");
    }

    public float getAnimatedAngle(float partialTicks) {
        return clientLerpedAngle.getValue(partialTicks);
    }

    public void setClientAngle(double clientAngle) {
        this.clientAngle = (float) clientAngle;
    }

    @Override
    public Direction getAlign() {return getDirection();}

    @Override
    public Direction getForward() {return getServoDirection();}
    /*
     * @returns:  indicates the positive rotational direction vector in ship-coordinate
     * */
    public abstract Direction getServoDirection();

    public Vector3d getServoDirectionJOML(){
        return ValkyrienSkies.set(new Vector3d(), getServoDirection().getNormal());
    }

    public abstract BlockPos getAssembleBlockPos();

    public abstract Vector3d getRotationCenterPosJOML();

    public double getServoAngle(){
        if(noCompanionShip())return 0;
        Matrix3dc own = readSelf().rotationMatrix().transpose(new Matrix3d()); //wc2sc
        Matrix3dc cmp = readComp().rotationMatrix().transpose(new Matrix3d());
        return VSMathUtils.get_yc2xc(own, cmp, getServoDirection(), getCompanionShipAlign());
    }

    public double getServoAngularSpeed(){
        if(noCompanionShip())return 0;
        ShipPhysics own = readSelf();
        ShipPhysics cmp = readComp();

        Matrix3dc m_own = own.rotationMatrix().transpose(new Matrix3d()); //wc2sc
        Vector3dc w_own = own.omega();
        Vector3dc w_cmp = cmp.omega();
        return VSMathUtils.get_dyc2xc(m_own, w_own, w_cmp,  getServoDirection(), getCompanionShipAlign());
    }

    public void assemble(){
        if(level == null || level.isClientSide)return;
        // var self = Optional.ofNullable(getShipOn());
        // if(self == null)return;
        ServerLevel serverLevel = (ServerLevel) level;
        List<BlockPos> collected = List.of(getAssembleBlockPos());
        ServerShip comp = ShipAssembler.INSTANCE.assembleToShip(serverLevel, collected.get(0), true, 1, true);
        if(comp == null)return;

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
        Vector3dc p_self = getRotationCenterPosJOML();
        Vector3dc p_comp = comp.getTransform().getPositionInShip().add(new Vector3d(0.5, 0.5, 0.5), new Vector3d());  // new Vector3d();
        // Vector3dc selfOffset = self.map(s -> s.getTransform().getPositionInShip()).orElse(); //.sub(p_self, new Vector3d())
        Quaterniondc q_self = VSMathUtils.getQuaternionToEast_(getServoDirection());
        Quaterniondc q_comp = VSMathUtils.getQuaternionToEast_(getServoDirection());


        Vector3d direction = getServoDirectionJOML();

        VSHingeOrientationConstraint hingeConstraint = new VSHingeOrientationConstraint(
                selfId,
                compId,
                1.0E-20,
                q_self,
                q_comp,
                1.0E20
        );

        VSAttachmentConstraint attachment_1 = new VSAttachmentConstraint(
                selfId,
                compId,
                1.0E-20,
                new Vector3d(p_self).fma(0, direction),
                new Vector3d(p_comp).fma(0, direction),
                1.0E20,
                0.0
        );

        VSAttachmentConstraint attachment_2 = new VSAttachmentConstraint(
                selfId,
                compId,
                1.0E-20,
                new Vector3d(p_self).fma(-2, direction),
                new Vector3d(p_comp).fma(-2, direction),
                1.0E20,
                0.0
        );
        /*
        VSRevoluteJoint joint = new VSRevoluteJoint(
                selfId,
                new VSJointPose(p_self, q_self),
                compId,
                new VSJointPose(p_comp, q_comp),
                new VSJointMaxForceTorque(1e20f, 1e20f),
                null, null, null, null, null
        );
        * */


        recreateRevoluteConstraints(hingeConstraint, attachment_1, attachment_2);
        setCompanionShipID(compId);
        setCompanionShipDirection(getServoDirection().getOpposite());
        setBlockConnectContext(new BlockPos(toMinecraft(new Vector3i().set(p_comp))));
        setStartingAngleOfCompanionShip();
        setChanged();
    }

    public void invalidateConnectContext(){
        context = ConnectContext.EMPTY;
    }

    public void updateConnectContext(){
        Optional
            .ofNullable(getConstraint("revolute")).ifPresentOrElse(
                rvl -> Optional.ofNullable(getConstraint("attach_1")).ifPresentOrElse(
                att -> {
                    VSHingeOrientationConstraint revolute = (VSHingeOrientationConstraint) rvl;
                    VSAttachmentConstraint attach = (VSAttachmentConstraint) att;
                    Vector3dc p_0 = attach.getLocalPos0();
                    Vector3dc p_1 = attach.getLocalPos1();
                    Quaterniondc q_0 = revolute.getLocalRot0();
                    Quaterniondc q_1 = revolute.getLocalRot1();
                    context = new ConnectContext(
                        new VSJointPose(p_0, q_0),
                        new VSJointPose(p_1, q_1),
                        false
                    );
                },
                        this::invalidateConnectContext
                ),
                        this::invalidateConnectContext
                );
    }

    public void recreateRevoluteConstraints(VSConstraint... joint){
        if(joint.length < 3){
            ControlCraft.LOGGER.error("Failed to recreate revolute constraints: invalid joint data");
            return;
        }
        overrideConstraint("revolute", joint[0]);
        overrideConstraint("attach_1", joint[1]);
        overrideConstraint("attach_2", joint[2]);
        updateConnectContext();
    }

    @Override
    public void destroyConstraints() {
        clearCompanionShipInfo();
        removeConstraint("revolute");
        removeConstraint("attach_1");
        removeConstraint("attach_2");
    }

    @Override
    public void bruteDirectionalConnectWith(BlockPos bp_comp, Direction align, Direction direction_comp) {
        Direction direction_serv = getServoDirection();///.getOpposite();
        Ship compShip = ValkyrienSkies.getShipManagingBlock(level, bp_comp);
        if(compShip == null)return;
        long selfId = getShipOrGroundID();
        long compId = compShip.getId();
        Quaterniondc q_self = VSMathUtils.getQuaternionToEast_(direction_serv);
        Quaterniondc q_comp = VSMathUtils.getQuaternionToEast_(direction_comp.getOpposite());

        Vector3dc p_self = getRotationCenterPosJOML();
        Vector3dc p_comp = ValkyrienSkies.set(new Vector3d(), bp_comp.getCenter());
        Vector3d dir_self = getServoDirectionJOML();
        Vector3d dir_comp = ValkyrienSkies.set(new Vector3d(), direction_comp.getNormal());
        /*
        VSRevoluteJoint joint = new VSRevoluteJoint(
                selfId,
                new VSJointPose(p_self, q_self),
                compId,
                new VSJointPose(p_comp, q_comp),
                new VSJointMaxForceTorque(1e20f, 1e20f),
                null, null, null, null, null
        );
        * */


        VSHingeOrientationConstraint hingeConstraint = new VSHingeOrientationConstraint(
                selfId,
                compId,
                1.0E-20,
                q_self,
                q_comp,
                1.0E20
        );

        VSAttachmentConstraint attachment_1 = new VSAttachmentConstraint(
                selfId,
                compId,
                1.0E-20,
                new Vector3d(p_self).fma(0, dir_self),
                new Vector3d(p_comp).fma(0, dir_comp),
                1.0E20,
                0.0
        );

        VSAttachmentConstraint attachment_2 = new VSAttachmentConstraint(
                selfId,
                compId,
                1.0E-20,
                new Vector3d(p_self).fma(-2, dir_self),
                new Vector3d(p_comp).fma(2, dir_comp),
                1.0E20,
                0.0
        );

        recreateRevoluteConstraints(hingeConstraint, attachment_1, attachment_2);
        setCompanionShipID(compId);
        setCompanionShipDirection(direction_comp);
        setBlockConnectContext(bp_comp);
        setStartingAngleOfCompanionShip();
        setChanged();

    }

    @Override
    public void tickServer() {
        super.tickServer();
        syncForNear(true, ANIMATED_ANGLE);
        // syncClientAnimation();
    }

    public void setOffset(Vector3dc offset) {
        this.offset = new Vector3d(offset);
        setChanged();
    }

    public void syncClientAnimation(){
        if(level != null && !level.isClientSide){
            var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.SYNC_0)
                    .withDouble(getServoAngle())
                    .build();
            ControlCraftPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    @Override
    public void tickClient() {
        super.tickClient();
        tickAnimation();
    }

    public void tickAnimation(){
        clientLerpedAngle.chase(Math.toDegrees(clientAngle), 0.5, LerpedFloat.Chaser.EXP);
        clientLerpedAngle.tickChaser();
    }

    public Vector3dc getOffset() {return offset;}

    public abstract void setStartingAngleOfCompanionShip();


    @Override
    protected void readExtra(CompoundTag compound) {
        VSchematicCompactCenter.PostMotorReadVModCompact(this, compound);
    }
}
