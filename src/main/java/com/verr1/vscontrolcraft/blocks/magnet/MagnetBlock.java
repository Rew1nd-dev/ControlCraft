package com.verr1.vscontrolcraft.blocks.magnet;

import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.deprecated.MagnetOpenScreenPacket;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import com.verr1.vscontrolcraft.registry.AllPackets;
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

public class MagnetBlock extends DirectionalBlock implements IBE<MagnetBlockEntity> {
    public static String ID = "magnet";

    public MagnetBlock(Properties p_52591_) {
        super(p_52591_);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    protected void displayScreen(MagnetBlockEntity entity, Player player){
        AllPackets.sendToPlayer(
                new MagnetOpenScreenPacket(entity.getBlockPos(), entity.getStrength()),
                ((ServerPlayer)player)
        );
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
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public Class<MagnetBlockEntity> getBlockEntityClass() {
        return MagnetBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MagnetBlockEntity> getBlockEntityType() {
        return AllBlockEntities.MAGNET_BLOCKENTITY.get();
    }
}
