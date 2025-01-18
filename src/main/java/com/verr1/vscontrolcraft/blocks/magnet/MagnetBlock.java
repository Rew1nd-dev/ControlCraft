package com.verr1.vscontrolcraft.blocks.magnet;

import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MagnetBlock extends DirectionalBlock implements IBE<MagnetBlockEntity> {
    public static String ID = "magnet";

    public MagnetBlock(Properties p_52591_) {
        super(p_52591_);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public Class<MagnetBlockEntity> getBlockEntityClass() {
        return MagnetBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MagnetBlockEntity> getBlockEntityType() {
        return AllBlockEntities.MAGNET_BLOCKENTITY.get();
    }
}
