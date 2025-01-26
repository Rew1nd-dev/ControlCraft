package com.verr1.vscontrolcraft.blocks.revoluteJoint;

import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.base.Hinge.HingeAdjustLevel;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.verr1.vscontrolcraft.registry.AllShapes.DIRECTIONAL_ROD;
import static com.verr1.vscontrolcraft.registry.AllShapes.FLAT_BASE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RevoluteJointBlock extends DirectionalAxisKineticBlock implements IBE<RevoluteJointBlockEntity> {
    public static final String ID = "revolute_joint";


    public static final EnumProperty<HingeAdjustLevel> LEVEL = EnumProperty.create("hinge_level", HingeAdjustLevel.class);
    public static final BooleanProperty Flipped = BooleanProperty.create("joint_direction_flipped");

    public RevoluteJointBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL);
        builder.add(Flipped);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return FLAT_BASE.get(state.getValue(FACING));
    }


    @Override
    public Class<RevoluteJointBlockEntity> getBlockEntityClass() {
        return RevoluteJointBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RevoluteJointBlockEntity> getBlockEntityType() {
        return AllBlockEntities.REVOLUTE_JOINT_BLOCKENTITY.get();
    }
}
