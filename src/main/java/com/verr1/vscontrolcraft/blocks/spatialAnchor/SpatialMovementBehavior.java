package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.blocks.jointMotor.JointMotorBlock;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class SpatialMovementBehavior implements MovementBehaviour {


    public Direction getAlign(BlockState state){
        return state.getValue(SpatialAnchorBlock.FACING);
    }

    public Direction getForward(BlockState state){
        boolean isFlipped = state.getValue(SpatialAnchorBlock.FLIPPED);
        return isFlipped ? getVerticalUnflipped(state).getOpposite() : getVerticalUnflipped(state);
    }

    public Direction getVerticalUnflipped(BlockState state){
        Direction facing = state.getValue(JointMotorBlock.FACING);
        Boolean align = state.getValue(JointMotorBlock.AXIS_ALONG_FIRST_COORDINATE);
        if(facing.getAxis() != Direction.Axis.X){
            if(align)return Direction.EAST;
            return facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        }
        if(align)return Direction.UP;
        return Direction.SOUTH;
    }

    public long getServerShipID(Vec3 p, ServerLevel level){
        return VSMathUtils.getServerShipID(BlockPos.containing(p), level);
    }

    public long getProtocol(MovementContext context){
        return context.blockEntityData.getLong("protocol");
    }

    public String getDimensionID(ServerLevel level){
        return VSMathUtils.getDimensionID(level);
    }


    public BlockPos getOriginalBlockPos(MovementContext context) {
        int x = context.blockEntityData.getInt("x");
        int y = context.blockEntityData.getInt("y");
        int z = context.blockEntityData.getInt("z");
        return new BlockPos(x, y, z);
    }

    public LogicalActorSpatial getLogicalActorSpatial(MovementContext context){
        return new LogicalActorSpatial(
                (ServerLevel) context.world,
                BlockPos.containing(context.position),
                getAlign(context.state),
                getForward(context.state),
                getServerShipID(context.position, (ServerLevel)context.world),
                getDimensionID((ServerLevel) context.world),
                getProtocol(context),
                getOriginalBlockPos(context),
                context.rotation,
                context.position
        );
    }

    public void activate(MovementContext context){
        SpatialLinkManager.activate(getLogicalActorSpatial(context));
    }


    @Override
    public void tick(MovementContext context) {
        if(context.world.isClientSide)return;
        if(!isActive(context))return;
        activate(context);
    }
}
