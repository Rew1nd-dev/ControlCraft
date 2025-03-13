package com.verr1.controlcraft.content.blocks.spatial;

import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.logical.LogicalActorSpatial;
import com.verr1.controlcraft.foundation.managers.SpatialLinkManager;
import com.verr1.controlcraft.utils.VSGetterUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;

import static com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class SpatialMovementBehavior implements MovementBehaviour {


    public Direction getAlign(BlockState state){
        return state.getValue(FACING);
    }

    public Direction getForward(BlockState state){
        boolean isFlipped = state.getValue(SpatialAnchorBlock.FLIPPED);
        return isFlipped ? getVerticalUnflipped(state).getOpposite() : getVerticalUnflipped(state);
    }

    public Direction getVerticalUnflipped(BlockState state){
        Direction facing = state.getValue(FACING);
        Boolean align = state.getValue(AXIS_ALONG_FIRST_COORDINATE);
        if(facing.getAxis() != Direction.Axis.X){
            if(align)return Direction.EAST;
            return facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        }
        if(align)return Direction.UP;
        return Direction.SOUTH;
    }

    public long getServerShipID(Vec3 p, ServerLevel level){
        return VSGetterUtils.getShip(level, BlockPos.containing(p)).map(Ship::getId).orElse(-1L);
    }

    public long getProtocol(MovementContext context){
        return context.blockEntityData.getLong("protocol");
    }

    public String getDimensionID(ServerLevel level){
        return ValkyrienSkies.getDimensionId(level);
    }


    public BlockPos getOriginalBlockPos(MovementContext context) {
        int x = context.blockEntityData.getInt("x");
        int y = context.blockEntityData.getInt("y");
        int z = context.blockEntityData.getInt("z");
        return new BlockPos(x, y, z);
    }

    public LogicalActorSpatial getLogicalActorSpatial(MovementContext context){
        return new LogicalActorSpatial(
                WorldBlockPos.of(context.world, BlockPos.containing(context.position)),
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
