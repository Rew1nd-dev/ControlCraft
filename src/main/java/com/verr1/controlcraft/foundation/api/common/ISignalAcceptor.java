package com.verr1.controlcraft.foundation.api.common;

import com.verr1.controlcraft.foundation.api.delegate.ITerminalDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface ISignalAcceptor {
    default void onNeighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                   boolean isMoving)  {
        if(worldIn.isClientSide)return;
        Direction direction = Direction.fromDelta(
                fromPos.getX() - pos.getX(),
                fromPos.getY() - pos.getY(),
                fromPos.getZ() - pos.getZ()
        );

        if(direction == null)return;
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if(!(blockEntity instanceof ITerminalDevice device))return;
        device.accept(worldIn.getSignal(pos.relative(direction), direction), direction);
    }
}
