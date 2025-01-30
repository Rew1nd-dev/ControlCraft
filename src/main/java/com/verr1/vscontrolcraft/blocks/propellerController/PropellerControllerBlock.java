package com.verr1.vscontrolcraft.blocks.propellerController;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

public class PropellerControllerBlock extends DirectionalBlock implements IBE<PropellerControllerBlockEntity> {

    public static final String ID = "propeller_controller";

    public PropellerControllerBlock(Properties properties) {
        super(properties);
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {

        return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    public Class<PropellerControllerBlockEntity> getBlockEntityClass() {
        return PropellerControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PropellerControllerBlockEntity> getBlockEntityType() {
        return AllBlockEntities.PROPELLER_CONTROLLER_BLOCKENTITY.get();
    }
}
