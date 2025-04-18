package com.verr1.controlcraft.content.blocks.motor;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.controlcraft.content.compact.vmod.CopyableMotor;
import com.verr1.controlcraft.content.gui.factory.GenericUIFactory;
import com.verr1.controlcraft.foundation.api.common.ISignalAcceptor;
import com.verr1.controlcraft.registry.ControlCraftBlockEntities;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import com.verr1.controlcraft.utils.MinecraftUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

import static com.verr1.controlcraft.registry.ControlCraftShapes.HALF_BOX_BASE;

public class KinematicRevoluteMotorBlock extends DirectionalAxisKineticBlock implements
        IBE<KinematicRevoluteMotorBlockEntity>, IWrenchable, ISignalAcceptor, CopyableMotor
{
    public static final String ID = "constraint_revolute";

    public KinematicRevoluteMotorBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving)  {
        ISignalAcceptor.super.onNeighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return HALF_BOX_BASE.get(state.getValue(FACING));
    }

    @OnlyIn(Dist.CLIENT)
    protected void displayScreen(BlockPos pos){
        ScreenOpener.open(GenericUIFactory.createKinematicDeviceScreen(pos, ControlCraftBlocks.CONSTRAINT_SERVO_MOTOR_BLOCK.asStack()));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {

        return face.getAxis() == MinecraftUtils.getVerticalDirection(state).getAxis() || face == state.getValue(FACING).getOpposite();
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(     worldIn.isClientSide
                && handIn == InteractionHand.MAIN_HAND
                && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                && !player.isShiftKeyDown()
        ) {
            displayScreen(pos);
            return InteractionResult.PASS;
        }
        if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && player.isShiftKeyDown()) {
            withBlockEntityDo(worldIn, pos, AbstractMotor::assemble);
        }
        return InteractionResult.PASS;
    }


    @Override
    public Class<KinematicRevoluteMotorBlockEntity> getBlockEntityClass() {
        return KinematicRevoluteMotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KinematicRevoluteMotorBlockEntity> getBlockEntityType() {
        return ControlCraftBlockEntities.CONSTRAINT_SERVO_MOTOR_BLOCK.get();
    }
}
