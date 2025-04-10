package com.verr1.controlcraft.content.blocks.slider;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.controlcraft.content.gui.factory.GenericUIFactory;
import com.verr1.controlcraft.foundation.api.ISignalAcceptor;
import com.verr1.controlcraft.registry.ControlCraftBlockEntities;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DynamicSliderBlock extends DirectionalKineticBlock
        implements IBE<DynamicSliderBlockEntity>, IWrenchable, ISignalAcceptor
{
    public static String ID = "slider";

    public DynamicSliderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving)  {
        ISignalAcceptor.super.onNeighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    @OnlyIn(Dist.CLIENT)
    protected void displayScreen(BlockPos pos){
        ScreenOpener.open(GenericUIFactory.createDynamicSliderScreen(pos, ControlCraftBlocks.SLIDER_CONTROLLER_BLOCK.asStack()));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(    worldIn.isClientSide
                && handIn == InteractionHand.MAIN_HAND
                && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                && !player.isShiftKeyDown()
        ) {
            displayScreen(pos);
            return InteractionResult.PASS;
        }
        if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && player.isShiftKeyDown()) {
            withBlockEntityDo(worldIn, pos, DynamicSliderBlockEntity::assemble);
        }
        return InteractionResult.PASS;
    }



    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public Class<DynamicSliderBlockEntity> getBlockEntityClass() {
        return DynamicSliderBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DynamicSliderBlockEntity> getBlockEntityType() {
        return ControlCraftBlockEntities.SLIDER_CONTROLLER_BLOCKENTITY.get();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(FACING).getOpposite() == face;
    }
}
