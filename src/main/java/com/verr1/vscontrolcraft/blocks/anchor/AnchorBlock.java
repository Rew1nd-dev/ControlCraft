package com.verr1.vscontrolcraft.blocks.anchor;

import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.deprecated.AnchorOpenScreenPacket;
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
import org.jetbrains.annotations.Nullable;

public class AnchorBlock extends DirectionalBlock implements IBE<AnchorBlockEntity> {
    public static final String ID = "anchor";

    public AnchorBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }



    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    protected void displayScreen(AnchorBlockEntity entity, Player player){
        AllPackets.sendToPlayer(
                new AnchorOpenScreenPacket(entity.getAirResistance(), entity.getExtraGravity(), entity.getBlockPos()),
                (ServerPlayer)player
        );
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
    public Class<AnchorBlockEntity> getBlockEntityClass() {
        return AnchorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AnchorBlockEntity> getBlockEntityType() {
        return AllBlockEntities.ANCHOR_BLOCKENTITY.get();
    }
}
