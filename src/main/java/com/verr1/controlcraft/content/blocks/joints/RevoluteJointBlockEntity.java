package com.verr1.controlcraft.content.blocks.joints;

import com.verr1.controlcraft.foundation.ServerBlockEntityGetter;
import com.verr1.controlcraft.foundation.api.IFlippableJoint;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.type.JointLevel;
import com.verr1.controlcraft.utils.MinecraftUtils;
import com.verr1.controlcraft.utils.VSGetterUtils;
import com.verr1.controlcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.*;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.joints.VSJoint;
import org.valkyrienskies.core.apigame.joints.VSJointMaxForceTorque;
import org.valkyrienskies.core.apigame.joints.VSJointPose;
import org.valkyrienskies.core.apigame.joints.VSRevoluteJoint;

import java.lang.Math;

import static com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;


public class RevoluteJointBlockEntity extends AbstractJointBlockEntity implements
        IFlippableJoint
{


    public RevoluteJointBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        registerConstraintKey("revolute");
    }

    @Override
    public void destroyConstraints() {
        if(level == null || level.isClientSide)return;
        removeConstraint("revolute");
    }

    public Direction getJointDirection(){
        Direction unflipped = getJointDirectionUnflipped();
        return isFlipped() ? unflipped : unflipped.getOpposite();
    }

    private Direction getJointDirectionUnflipped(){
        Direction facing = getBlockState().getValue(FACING);
        Boolean align = getBlockState().getValue(AXIS_ALONG_FIRST_COORDINATE);
        if(facing.getAxis() != Direction.Axis.X){
            if(align)return Direction.EAST;
            return facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        }
        if(align)return Direction.UP;
        return Direction.SOUTH;
    }

    @Override
    public void bruteDirectionalConnectWith(BlockPos pos, Direction align, Direction forward) {
        if(level == null || level.isClientSide)return;
        Ship selfShip = getShipOn();
        Ship otherShip = VSGetterUtils.getShipOn(level, pos).orElse(null);
        if(otherShip == null || selfShip == null)return;
        RevoluteJointBlockEntity otherHinge = ServerBlockEntityGetter.INSTANCE.getBlockEntityAt((ServerLevel) level, pos, RevoluteJointBlockEntity.class).orElse(null);
        if(otherHinge == null)return;

        Vector3dc selfContact = getJointConnectorPosJOML();
        Vector3dc otherContact = otherHinge.getJointConnectorPosJOML();

        Quaterniondc selfRotation = new
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(getJointDirection()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();

        Quaterniondc otherRotation = new
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(otherHinge.getJointDirection().getOpposite()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();

        VSRevoluteJoint joint = new VSRevoluteJoint(
                selfShip.getId(),
                new VSJointPose(selfContact, selfRotation),
                otherShip.getId(),
                new VSJointPose(otherContact, otherRotation),
                new VSJointMaxForceTorque(1e20f, 1e20f),
                null, null, null, null, null
        );

        recreateConstraints(joint);

    }

    public void recreateConstraints(VSJoint joint){
        if(level == null || level.isClientSide)return;
        overrideConstraint("revolute", joint);

    }

    @Override
    public Direction getAlign() {
        return getDirection();
    }

    @Override
    public Direction getForward() {
        return MinecraftUtils.getVerticalDirectionSimple(getDirection());
    }

    @Override
    public boolean isFlipped(){
        return getBlockState().getValue(AbstractJointBlock.FLIPPED);
    }

    @Override
    public void setFlipped(boolean flipped) {
        MinecraftUtils.updateBlockState(level, getBlockPos(), getBlockState().setValue(AbstractJointBlock.FLIPPED, flipped));
    }
}
