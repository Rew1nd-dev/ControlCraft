package com.verr1.vscontrolcraft.blocks.annihilator;

import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class AnnihilatorBlock extends DirectionalBlock implements IBE<AnnihilatorBlockEntity> {

    public static final String ID = "annihilator";

    public AnnihilatorBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving)  {
        if(worldIn.isClientSide)return;
        if(worldIn.hasNeighborSignal(pos)){
            withBlockEntityDo(worldIn, pos, AnnihilatorBlockEntity::annihilate);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public Class<AnnihilatorBlockEntity> getBlockEntityClass() {
        return AnnihilatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AnnihilatorBlockEntity> getBlockEntityType() {
        return AllBlockEntities.ANNIHILATOR_BLOCKENTITY.get();
    }
}
