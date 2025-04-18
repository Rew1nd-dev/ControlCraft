package com.verr1.controlcraft.content.blocks.kinetic;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.controlcraft.registry.ControlCraftBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class KineticProxyBlock extends DirectionalKineticBlock implements IBE<KineticProxyBlockEntity> {

    public static final String ID = "kinetic_proxy";

    public KineticProxyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return true;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public Class<KineticProxyBlockEntity> getBlockEntityClass() {
        return KineticProxyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticProxyBlockEntity> getBlockEntityType() {
        return ControlCraftBlockEntities.KINETIC_PROXY_BLOCKENTITY.get();
    }
}
