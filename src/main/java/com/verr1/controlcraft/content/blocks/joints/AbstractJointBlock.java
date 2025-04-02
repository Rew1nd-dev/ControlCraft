package com.verr1.controlcraft.content.blocks.joints;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.type.JointLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.verr1.controlcraft.registry.ControlCraftShapes.FLAT_BASE;

public class AbstractJointBlock extends DirectionalAxisKineticBlock implements IWrenchable {


    public static final EnumProperty<JointLevel> LEVEL = EnumProperty.create("joint_level", JointLevel.class);
    public static final BooleanProperty FLIPPED = BooleanProperty.create("joint_direction_flipped");

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }


    public AbstractJointBlock(Properties properties) {
        super(properties);

    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return FLAT_BASE.get(state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(LEVEL, JointLevel.FULL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if(!(context.getLevel() instanceof ServerLevel level))return InteractionResult.SUCCESS;
        BlockEntityGetter.INSTANCE
                .getLevelBlockEntityAt(level, context.getClickedPos(), AbstractJointBlockEntity.class)
                .ifPresent(AbstractJointBlockEntity::adjust);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if(context.getLevel().isClientSide)return InteractionResult.SUCCESS;
        if(context.getClickedFace() != state.getValue(FACING))return InteractionResult.FAIL;
        if(state.getValue(FLIPPED)){
            super.onWrenched(state, context);
        }
        BlockEntityGetter.INSTANCE
                .getLevelBlockEntityAt((ServerLevel) context.getLevel(), context.getClickedPos(), RevoluteJointBlockEntity.class)
                .ifPresent(RevoluteJointBlockEntity::flip);


        return InteractionResult.PASS;
    }

}
