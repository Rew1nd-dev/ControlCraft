package com.verr1.controlcraft.content.blocks.terminal;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.controlcraft.registry.ControlCraftBlockEntities;
import com.verr1.controlcraft.registry.ControlCraftShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TerminalBlock extends DirectionalKineticBlock implements
        IBE<TerminalBlockEntity>, IWrenchable
{

    public static final String ID=  "terminal";

    public static final BooleanProperty HALF = BooleanProperty.create("half");

    public TerminalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return state.getValue(HALF) ? ControlCraftShapes.HALF_BOX_BASE.get(state.getValue(FACING)) : Shapes.block();
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving)  {
        if(worldIn.isClientSide)return;

        // withBlockEntityDo(worldIn, pos, be -> be.accept(worldIn.getBestNeighborSignal(pos)));

    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if(context.getLevel().isClientSide)return InteractionResult.PASS;
        context.getLevel().setBlock(context.getClickedPos(), state.setValue(HALF, !state.getValue(HALF)), 3);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(worldIn.isClientSide)return InteractionResult.PASS;
        if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && handIn.equals(InteractionHand.MAIN_HAND)){
            withBlockEntityDo(worldIn, pos, t -> t.openScreen(player));
        }
        return InteractionResult.PASS;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return null;
    }

    @Override
    public Class<TerminalBlockEntity> getBlockEntityClass() {
        return TerminalBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends TerminalBlockEntity> getBlockEntityType() {
        return ControlCraftBlockEntities.TERMINAL_BLOCKENTITY.get();
    }




}
