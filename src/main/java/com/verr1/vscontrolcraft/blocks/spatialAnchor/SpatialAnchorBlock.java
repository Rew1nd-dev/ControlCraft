package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.blocks.revoluteJoint.RevoluteJointBlockEntity;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class SpatialAnchorBlock extends DirectionalAxisKineticBlock implements
        IBE<SpatialAnchorBlockEntity>, IWrenchable
{

    public static final String ID = "spatial_anchor";

    public static final BooleanProperty FLIPPED = BooleanProperty.create("flipped");

    public SpatialAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FLIPPED);
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
        int signal = worldIn.getControlInputSignal(fromPos, direction.getOpposite(), false); // direct input
        withBlockEntityDo(worldIn, pos, be -> be.accept(worldIn.getSignal(fromPos, direction.getOpposite()), direction));

    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if(context.getClickedFace() != state.getValue(FACING))return super.onWrenched(state, context);
        if(state.getValue(FLIPPED)){
            super.onWrenched(state, context);
        }
        withBlockEntityDo(context.getLevel(), context.getClickedPos(), SpatialAnchorBlockEntity::flip);
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(worldIn.isClientSide)return InteractionResult.PASS;
        if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()){
            withBlockEntityDo(worldIn, pos, be -> be.displayScreen((ServerPlayer) player));
        }

        return InteractionResult.PASS;
    }

    @Override
    public Class<SpatialAnchorBlockEntity> getBlockEntityClass() {
        return SpatialAnchorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SpatialAnchorBlockEntity> getBlockEntityType() {
        return AllBlockEntities.SPATIAL_ANCHOR_BLOCKENTITY.get();
    }
}
