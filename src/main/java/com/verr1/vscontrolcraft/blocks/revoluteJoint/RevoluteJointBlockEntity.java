package com.verr1.vscontrolcraft.blocks.revoluteJoint;

import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.Constrain.ConstrainCenter;
import com.verr1.vscontrolcraft.base.Constrain.DataStructure.ConstrainKey;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.base.Hinge.*;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IAdjustableHinge;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.ICanBruteConnect;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IConstrainHolder;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IFlippableHinge;
import com.verr1.vscontrolcraft.base.Hinge.packets.HingeSyncClientPacket;
import com.verr1.vscontrolcraft.base.ShipConnectorBlockEntity;
import com.verr1.vscontrolcraft.blocks.jointMotor.JointMotorBlock;
import com.verr1.vscontrolcraft.blocks.sphericalHinge.SphericalHingeBlock;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSConstrainSerializeUtils;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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

import java.util.Arrays;

public class RevoluteJointBlockEntity extends ShipConnectorBlockEntity implements
        ICanBruteConnect, IAdjustableHinge, IFlippableHinge, IConstrainHolder
{

    public RevoluteJointBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction getJointDirection(){
        Direction unflipped = getJointDirectionUnflipped();
        return isFlipped() ? unflipped : unflipped.getOpposite();
    }

    private Direction getJointDirectionUnflipped(){
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

    public HingeAdjustLevel getHingeLevel(){
        return getBlockState().getValue(SphericalHingeBlock.LEVEL);
    }

    @Override
    public void adjust() {
        setAdjustment(getHingeLevel().next());
        // syncToClient();
    }

    @Override
    public HingeAdjustLevel getAdjustment() {
        return getHingeLevel();
    }

    @Override
    public void setAdjustment(HingeAdjustLevel hingeLevel) {
        updateBlockState(level, getBlockPos(), getBlockState().setValue(SphericalHingeBlock.LEVEL, hingeLevel));
    }


    void syncToClient(){
        if(level.isClientSide)return;
        // setChanged();

        AllPackets
                .getChannel()
                .send(PacketDistributor.ALL.noArg(),new HingeSyncClientPacket(getBlockPos(), getAdjustment(), isFlipped()));


    }

    private Vector3d getHingeConnectorPosJOML() {
        return Util.Vec3toVector3d(getBlockPos().getCenter())
                .fma(-0.5, getDirectionJOML())
                .fma(getHingeLevel().correspondLength(), getDirectionJOML());
    }



    @Override
    public void bruteConnectWith(BlockPos otherHingeBlockPos) {
        if(level.isClientSide)return;
        if(!VSMathUtils.isOnServerShip(otherHingeBlockPos, (ServerLevel) level) && !isOnServerShip())return;
        if(!(level.getExistingBlockEntity(otherHingeBlockPos) instanceof RevoluteJointBlockEntity otherHinge))return;

        boolean isCmpOnGround = !otherHinge.isOnServerShip();

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
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(otherHinge.getJointDirection()))
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

        recreateConstrains(attachment, orientation, isCmpOnGround);
        setCompanionShipID(otherHinge.getServerShipID());
        notifyUpdate();
    }

    public void recreateConstrains(VSAttachmentConstraint attach, VSHingeOrientationConstraint hinge, boolean isCmpOnGround)
    {
        var shipWorldCore = (ShipObjectServerWorld)VSGameUtilsKt.getShipObjectWorld((ServerLevel) level);

        boolean isGrounded = !isOnServerShip();

        ConstrainCenter.createOrReplaceNewConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "attach", isGrounded, isCmpOnGround, false),
                attach,
                shipWorldCore
        );

        ConstrainCenter.createOrReplaceNewConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "hinge", isGrounded, isCmpOnGround, false),
                hinge,
                shipWorldCore
        );
    }

    @Override
    public void destroyConstrain() {
        boolean isGrounded = !isOnServerShip();
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "attach", isGrounded, false, false));
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "hinge", isGrounded, false, false));
        clearCompanionShipInfo();
    }

    @Override
    public void flip() {
        setFlipped(!isFlipped());
        // syncToClient();  // no need for blockstate update
    }

    @Override
    public boolean isFlipped(){
        return getBlockState().getValue(RevoluteJointBlock.Flipped);
    }

    @Override
    public void setFlipped(boolean flipped) {
        updateBlockState(level, getBlockPos(), getBlockState().setValue(RevoluteJointBlock.Flipped, flipped));
    }


    @Override
    public void destroy() {
        super.destroy();
        if(level.isClientSide)return;
        destroyConstrain();
    }


}
