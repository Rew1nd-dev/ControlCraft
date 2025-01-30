package com.verr1.vscontrolcraft.blocks.jet;

import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

public class JetBlock extends DirectionalBlock implements IBE<JetBlockEntity> {

    public static final String ID = "jet";

    public JetBlock(Properties p_52591_) {
        super(p_52591_);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public Class<JetBlockEntity> getBlockEntityClass() {
        return JetBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends JetBlockEntity> getBlockEntityType() {
        return AllBlockEntities.JET_BLOCKENTITY.get();
    }
}
