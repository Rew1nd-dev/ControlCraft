package com.verr1.controlcraft.content.blocks.joints;

import com.verr1.controlcraft.foundation.ServerBlockEntityGetter;
import com.verr1.controlcraft.utils.VSGetterUtils;
import com.verr1.controlcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.AxisAngle4d;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.joints.*;

public class FreeJointBlockEntity extends AbstractJointBlockEntity{
    public FreeJointBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        registerConstraintKey("fix");
    }

    @Override
    public void destroyConstraints() {
        removeConstraint("fix");
    }

    @Override
    public void bruteDirectionalConnectWith(BlockPos pos, Direction align, Direction forward) {
        if(level == null || level.isClientSide)return;
        Ship selfShip = getShipOn();
        Ship otherShip = VSGetterUtils.getShipOn(level, pos).orElse(null);
        if(otherShip == null || selfShip == null)return;
        FreeJointBlockEntity otherHinge = ServerBlockEntityGetter.INSTANCE.getBlockEntityAt((ServerLevel) level, pos, FreeJointBlockEntity.class).orElse(null);
        if(otherHinge == null)return;

        Vector3dc selfContact = getJointConnectorPosJOML();
        Vector3dc otherContact = otherHinge.getJointConnectorPosJOML();


        VSSphericalJoint joint = new VSSphericalJoint(
                selfShip.getId(),
                new VSJointPose(selfContact, new Quaterniond()),
                otherShip.getId(),
                new VSJointPose(otherContact, new Quaterniond()),
                new VSJointMaxForceTorque(1e20f, 1e20f),
                null
        );

        recreateConstraints(joint);
    }

    public void recreateConstraints(VSJoint joint){
        if(level == null || level.isClientSide)return;
        overrideConstraint("fix", joint);

    }

    @Override
    public Direction getAlign() {
        return null;
    }

    @Override
    public Direction getForward() {
        return null;
    }
}
