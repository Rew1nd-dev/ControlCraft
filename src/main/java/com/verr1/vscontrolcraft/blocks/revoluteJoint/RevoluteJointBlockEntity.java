package com.verr1.vscontrolcraft.blocks.revoluteJoint;

import com.verr1.vscontrolcraft.base.Hinge.HingeAdjustLevel;
import com.verr1.vscontrolcraft.base.Hinge.HingeSyncLevelPacket;
import com.verr1.vscontrolcraft.base.Hinge.IAdjustableHinge;
import com.verr1.vscontrolcraft.base.Hinge.ICanBruteConnect;
import com.verr1.vscontrolcraft.base.ShipConnectorBlockEntity;
import com.verr1.vscontrolcraft.blocks.jointMotor.JointMotorBlock;
import com.verr1.vscontrolcraft.blocks.sphereHinge.SphericalHingeBlock;
import com.verr1.vscontrolcraft.blocks.sphereHinge.SphericalHingeBlockEntity;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.joml.AxisAngle4d;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;
import org.valkyrienskies.core.apigame.constraints.VSHingeOrientationConstraint;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class RevoluteJointBlockEntity extends ShipConnectorBlockEntity implements ICanBruteConnect, IAdjustableHinge {

    private HingeAdjustLevel hingeLevel = HingeAdjustLevel.FULL;

    private VSAttachmentConstraint attach;
    private Object attach_ID;
    private VSHingeOrientationConstraint hinge;
    private Object hinge_ID;

    public RevoluteJointBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction getJointDirection(){
        Direction facing = getBlockState().getValue(JointMotorBlock.FACING);
        Boolean align = getBlockState().getValue(JointMotorBlock.AXIS_ALONG_FIRST_COORDINATE);
        if(facing.getAxis() != Direction.Axis.X){
            if(align)return Direction.EAST;
            return facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        }
        if(align)return Direction.UP;
        return Direction.SOUTH;
    }

    public Vector3d getJointDirectionJOML(){
        return Util.Vec3itoVector3d(getJointDirection().getNormal());
    }

    @Override
    public void adjust() {
        hingeLevel = hingeLevel.next();
    }

    @Override
    public HingeAdjustLevel getAdjustment() {
        return hingeLevel;
    }

    @Override
    public void setAdjustment(HingeAdjustLevel level) {
        hingeLevel = level;
    }


    void syncToClient(){
        AllPackets
                .getChannel()
                .send(PacketDistributor.ALL.noArg(),new HingeSyncLevelPacket(getBlockPos(), getAdjustment()));
    }

    private Vector3d getHingeConnectorPosJOML() {
        return Util.Vec3toVector3d(getBlockPos().getCenter())
                .fma(-0.5, getDirectionJOML())
                .fma(hingeLevel.correspondLength(), getDirectionJOML());
    }



    @Override
    public void bruteConnectWith(BlockPos otherHingeBlockPos) {
        if(level.isClientSide)return;
        if(!VSMathUtils.isOnServerShip(otherHingeBlockPos, (ServerLevel) level) && !isOnServerShip())return;
        if(!(level.getExistingBlockEntity(otherHingeBlockPos) instanceof RevoluteJointBlockEntity otherHinge))return;


        VSAttachmentConstraint attachment = new VSAttachmentConstraint(
                getServerShipID(),
                otherHinge.getServerShipID(),
                1.0E-10,
                getHingeConnectorPosJOML(),
                otherHinge.getHingeConnectorPosJOML(),
                1.0E10,
                0.0
        );

        Quaterniondc hingeQuaternion_Own = new
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(getJointDirection()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();

        Quaterniondc hingeQuaternion_Asm = new
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(otherHinge.getJointDirection().getOpposite()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();

        VSHingeOrientationConstraint orientation = new VSHingeOrientationConstraint(
                getServerShipID(),
                otherHinge.getServerShipID(),
                1.0E-10,
                hingeQuaternion_Own,
                hingeQuaternion_Asm,
                1.0E10
        );

        recreateConstrains(attachment, orientation);
        setCompanionShipID(otherHinge.getServerShipID());
        notifyUpdate();
    }

    public void recreateConstrains(VSAttachmentConstraint attach, VSHingeOrientationConstraint hinge)
    {
        this.attach = attach;
        this.hinge = hinge;
        recreateConstrains();
    }

    public void recreateConstrains(){
        if(attach == null || hinge == null)return;
        if(level.isClientSide)return;
        var shipWorldCore = (ShipObjectServerWorld) VSGameUtilsKt.getShipObjectWorld((ServerLevel) level);
        attach_ID =  shipWorldCore.createNewConstraint(this.attach);
        hinge_ID = shipWorldCore.createNewConstraint(this.hinge);
        if(attach_ID == null || hinge_ID == null){
            attach = null;
            hinge = null;
            attach_ID = null;
            hinge_ID = null;
        }
    }
}
