package com.verr1.controlcraft.content.blocks.motor;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.controlcraft.content.compact.vmod.CopyableMotor;
import com.verr1.controlcraft.content.gui.factory.GenericUIFactory;
import com.verr1.controlcraft.foundation.api.common.ISignalAcceptor;
import com.verr1.controlcraft.registry.ControlCraftBlockEntities;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static com.verr1.controlcraft.registry.ControlCraftShapes.HALF_BOX_BASE;

public class DynamicRevoluteMotorBlock extends DirectionalKineticBlock implements
        IBE<DynamicRevoluteMotorBlockEntity>, ISignalAcceptor, CopyableMotor
{

    public static String ID = "servo";

    public DynamicRevoluteMotorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(FACING).getOpposite() == face;
    }

    @OnlyIn(Dist.CLIENT)
    protected void displayScreen(BlockPos pos){
        ScreenOpener.open(GenericUIFactory.createDynamicMotorScreen(pos, ControlCraftBlocks.SERVO_MOTOR_BLOCK.asStack()));
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving)  {
        ISignalAcceptor.super.onNeighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(    worldIn.isClientSide
            && handIn == InteractionHand.MAIN_HAND
            && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
            && !player.isShiftKeyDown()
        ) {
            displayScreen(pos);
            return InteractionResult.PASS;
        }
        if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && player.isShiftKeyDown()) {
            withBlockEntityDo(worldIn, pos, DynamicRevoluteMotorBlockEntity::assemble);
        }
        return InteractionResult.PASS;
    }


    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return HALF_BOX_BASE.get(state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }


    @Override
    public Class<DynamicRevoluteMotorBlockEntity> getBlockEntityClass() {
        return DynamicRevoluteMotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DynamicRevoluteMotorBlockEntity> getBlockEntityType() {
        return ControlCraftBlockEntities.SERVO_MOTOR_BLOCKENTITY.get();
    }
}
