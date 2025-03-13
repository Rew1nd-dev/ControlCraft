package com.verr1.controlcraft.utils;

import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Optional;

public class MinecraftUtils {
    public static void updateBlockState(@Nullable Level world, BlockPos pos, BlockState newState){
        Optional.ofNullable(world).ifPresent(w -> w.setBlock(pos, newState, 3));
    }

    @OnlyIn(Dist.CLIENT)
    public static @Nullable Direction lookingAtFaceDirection(){
        return Optional
                .ofNullable(Minecraft.getInstance().player)
                .map(player -> player.pick(5, Minecraft.getInstance().getPartialTick(), false))
                .filter(hitResult -> hitResult.getType() == HitResult.Type.BLOCK)
                .map(hitResult -> (BlockHitResult) hitResult)
                .map(BlockHitResult::getDirection)
                .orElse(null);
    }

    @OnlyIn(Dist.CLIENT)
    public static @Nullable BlockEntity lookingAt(){
        Minecraft mc = Minecraft.getInstance();
        return Optional
                .ofNullable(mc.player)
                .map(player -> player.pick(5, mc.getPartialTick(), false))
                .filter(BlockHitResult.class::isInstance)
                .map(BlockHitResult.class::cast)
                .map(BlockHitResult::getBlockPos)
                .flatMap(
                    p -> Optional
                            .ofNullable(mc.level)
                            .map(level -> level.getBlockEntity(p)))
                .orElse(null);
    }

    public static Direction getVerticalDirectionSimple(Direction facing){
        if(facing.getAxis() != Direction.Axis.Y)return Direction.UP;
        return facing.getClockWise(Direction.Axis.Y);
    }

    public static Direction getVerticalDirection(BlockState state){
        if(!state.hasProperty(BlockStateProperties.FACING) ||
                !state.hasProperty(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE))return Direction.UP;

        Direction facing = state.getValue(BlockStateProperties.FACING);
        Boolean align = state.getValue(DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE);
        if(facing.getAxis() != Direction.Axis.X){
            if(align)return Direction.EAST;
            return facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        }
        if(align)return Direction.UP;
        return Direction.SOUTH;
    }

}
