package com.verr1.vscontrolcraft.blocks.recevier;

import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralRunnable;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.verr1.vscontrolcraft.registry.AllShapes.HALF_BOX_BASE;


public class ReceiverBlock extends DirectionalBlock implements IBE<ReceiverBlockEntity> {

    public static final String ID = "receiver";

    public ReceiverBlock(Properties p_52591_) {
        super(p_52591_);
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        IBE.onRemove(state, worldIn, pos, newState);
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
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if(world.isClientSide)return;
        DeferralExecutor.executeLater(new UpdateAttachedPeripheralLater(10, pos, world));
    }


    protected void displayScreen(ReceiverBlockEntity entity, Player player){

        PeripheralKey networkKey = entity.getNetworkKey();
        String peripheralType = entity.getAttachedPeripheralType();
        AllPackets.sendToPlayer(
                new ReceiverOpenScreenPacket(entity.getBlockPos(), networkKey.Name(), peripheralType ,networkKey.Protocol()),
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
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block otherBlock, BlockPos neighborPos,
                                boolean isMoving) {
        if(world.isClientSide)return;
        withBlockEntityDo(world, pos, ReceiverBlockEntity::updateAttachedPeripheral);
    }

    @Override
    public Class<ReceiverBlockEntity> getBlockEntityClass() {
        return ReceiverBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ReceiverBlockEntity> getBlockEntityType() {
        return AllBlockEntities.RECEIVER_BLOCKENTITY.get();
    }


    private static class UpdateAttachedPeripheralLater implements DeferralRunnable{
        int deferralTick;
        BlockPos pos;
        Level level;

        UpdateAttachedPeripheralLater(int tick, BlockPos pos, Level level){
            deferralTick = tick;
            this.pos = pos;
            this.level = level;
        }

        @Override
        public int getDeferralTicks() {
            return deferralTick;
        }

        @Override
        public void tickDown() {
            deferralTick--;
        }

        @Override
        public void run() {
            BlockEntity entity = level.getExistingBlockEntity(pos);
            if(entity instanceof ReceiverBlockEntity){
                ((ReceiverBlockEntity)entity).updateAttachedPeripheral();
            }
        }
    }

}
