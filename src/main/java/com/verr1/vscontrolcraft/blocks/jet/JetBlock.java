package com.verr1.vscontrolcraft.blocks.jet;

import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class JetBlock extends DirectionalBlock implements IBE<JetBlockEntity> {

    public static final String ID = "jet";

    public JetBlock(Properties p_52591_) {
        super(p_52591_);
    }


    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving)  {
        if(worldIn.isClientSide)return;
        Direction direction = Direction.fromDelta(
                fromPos.getX() - pos.getX(),
                fromPos.getY() - pos.getY(),
                fromPos.getZ() - pos.getZ()
        );
        if(direction == null)return;
        withBlockEntityDo(worldIn, pos, be -> be.accept(worldIn.getSignal(fromPos, direction.getOpposite()), direction));

    }


    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(worldIn.isClientSide)return InteractionResult.PASS;
        if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()){
            withBlockEntityDo(worldIn, pos, be -> be.displayScreen((ServerPlayer)player));
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if(context.getPlayer() != null && context.getPlayer().isShiftKeyDown())face = face.getOpposite();
        return defaultBlockState().setValue(FACING, face);
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
