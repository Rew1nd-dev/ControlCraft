package com.verr1.vscontrolcraft.blocks.terminal;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import com.verr1.vscontrolcraft.registry.AllShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TerminalBlock extends DirectionalKineticBlock implements
        IBE<TerminalBlockEntity>, IWrenchable
{

    public static final String ID=  "terminal";

    public TerminalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return AllShapes.HALF_BOX_BASE.get(state.getValue(FACING));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(worldIn.isClientSide)return InteractionResult.PASS;
        if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()){
            BlockEntity be = worldIn.getBlockEntity(pos);
            if(!(be instanceof TerminalBlockEntity terminal))return InteractionResult.FAIL;
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
        return AllBlockEntities.TERMINAL_BLOCKENTITY.get();
    }




}
