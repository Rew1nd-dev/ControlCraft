package com.verr1.vscontrolcraft.blocks.pivotJoint;

import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.base.Hinge.HingeAdjustLevel;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.verr1.vscontrolcraft.registry.AllShapes.FLAT_BASE;

public class PivotJointBlock extends DirectionalBlock implements IBE<PivotJointBlockEntity> {
    public static final String ID = "pivot";

    public static final EnumProperty<HingeAdjustLevel> LEVEL = EnumProperty.create("hinge_level", HingeAdjustLevel.class);

    public PivotJointBlock(Properties p_52591_) {
        super(p_52591_);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return FLAT_BASE.get(state.getValue(FACING));
    }

    @Override
    public Class<PivotJointBlockEntity> getBlockEntityClass() {
        return PivotJointBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PivotJointBlockEntity> getBlockEntityType() {
        return AllBlockEntities.PIVOT_JOINT_BLOCKENTITY.get();
    }
}
