package com.verr1.vscontrolcraft.blocks.spinalyzer;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import com.verr1.vscontrolcraft.registry.AllBlockStates;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;


public class SpinalyzerBlock extends DirectionalBlock implements IBE<SpinalyzerBlockEntity>, IWrenchable {
    public static final String ID = "spinalyzer";


    public SpinalyzerBlock(Properties p_52591_) {
        super(p_52591_);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace(); // 方块被点击的面


        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if(context.getClickedFace() != state.getValue(FACING)){
            // BlockState newState = state.setValue(ROTATION,(state.getValue(ROTATION) + 1) % 4);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Class<SpinalyzerBlockEntity> getBlockEntityClass() {
        return SpinalyzerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SpinalyzerBlockEntity> getBlockEntityType() {
        return AllBlockEntities.SPINALYZER_BLOCKENTITY.get();
    }


    public static class SpinalyzerGenerator{

    }
}
