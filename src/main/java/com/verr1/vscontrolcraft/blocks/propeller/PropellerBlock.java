package com.verr1.vscontrolcraft.blocks.propeller;

import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PropellerBlock extends DirectionalBlock implements IBE<PropellerBlockEntity> {
    public static final String ID = "propeller";

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
        super.createBlockStateDefinition(builder);
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return AllShapes.LARGE_GEAR.get(p_60555_.getValue(FACING).getAxis());
    }

    protected void displayScreen(PropellerBlockEntity entity, Player player){

        double thrustRatio = entity.getThrustRatio();
        double torqueRatio = entity.getTorqueRatio();
        boolean reverseTorque = entity.getReverseTorque();
        AllPackets.sendToPlayer(
                new PropellerOpenScreenPacket(entity.getBlockPos(), reverseTorque, thrustRatio, torqueRatio),
                ((ServerPlayer)player)
        );

    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(worldIn.isClientSide)return InteractionResult.PASS;
        withBlockEntityDo(worldIn, pos, be -> this.displayScreen(be, player));
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
