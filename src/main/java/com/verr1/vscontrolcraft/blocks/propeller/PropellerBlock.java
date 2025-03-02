package com.verr1.vscontrolcraft.blocks.propeller;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.verr1.vscontrolcraft.registry.AllShapes.HALF_BOX_BASE;

public class PropellerBlock extends DirectionalBlock
        implements IBE<PropellerBlockEntity>, IWrenchable
{
    public static final String ID = "propeller";

    public static final BooleanProperty HAS_BLADES = BooleanProperty.create("has_blades");


    public PropellerBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(HAS_BLADES);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if(context.getLevel().isClientSide)return InteractionResult.SUCCESS;
        context.getLevel().setBlock(context.getClickedPos(), state.cycle(HAS_BLADES), 3);
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return HALF_BOX_BASE.get(state.getValue(FACING));
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
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        //ControlCraftMod.LOGGER.info("ChunkLoaderBlock.onRemove called at" + pos.toString());
        IBE.onRemove(state, worldIn, pos, newState);
    }

    @Override
    public Class<PropellerBlockEntity> getBlockEntityClass() {
        return PropellerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PropellerBlockEntity> getBlockEntityType() {
        return AllBlockEntities.PROPELLER_BLOCKENTITY.get();
    }
}
