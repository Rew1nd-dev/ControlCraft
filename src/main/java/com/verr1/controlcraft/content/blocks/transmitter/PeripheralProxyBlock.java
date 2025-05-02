package com.verr1.controlcraft.content.blocks.transmitter;

import com.simibubi.create.foundation.block.IBE;
import com.verr1.controlcraft.registry.ControlCraftBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.verr1.controlcraft.registry.ControlCraftShapes.HALF_BOX_BASE;

public class PeripheralProxyBlock extends DirectionalBlock implements IBE<PeripheralProxyBlockEntity> {
    public static final String ID = "transmitter";

    public PeripheralProxyBlock(Properties p_52591_) {
        super(p_52591_);
    }




    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return HALF_BOX_BASE.get(state.getValue(FACING));
    }

    @Override
    public Class<PeripheralProxyBlockEntity> getBlockEntityClass() {
        return PeripheralProxyBlockEntity.class;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        //ControlCraftMod.LOGGER.info("ChunkLoaderBlock.onRemove called at" + pos.toString());
        IBE.onRemove(state, worldIn, pos, newState);
    }

    @Override
    public BlockEntityType<? extends PeripheralProxyBlockEntity> getBlockEntityType() {
        return ControlCraftBlockEntities.TRANSMITTER_BLOCKENTITY.get();
    }
}
