package com.verr1.vscontrolcraft.blocks.jetRudder;

import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.blocks.jet.JetBlockEntity;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

public class JetRudderBlock extends DirectionalBlock implements IBE<JetRudderBlockEntity> {
    public JetRudderBlock(Properties p_52591_) {
        super(p_52591_);
    }

    public static final String ID = "jet_rudder";


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
    public Class<JetRudderBlockEntity> getBlockEntityClass() {
        return JetRudderBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends JetRudderBlockEntity> getBlockEntityType() {
        return AllBlockEntities.JET_RUDDER_BLOCKENTITY.get();
    }
}
