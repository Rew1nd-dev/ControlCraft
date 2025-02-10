package com.verr1.vscontrolcraft.blocks.slider;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.base.Servo.PID;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerOpenScreenPacket;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerType;
import com.verr1.vscontrolcraft.blocks.servoMotor.ServoMotorBlockEntity;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class SliderControllerBlock extends DirectionalKineticBlock implements
        IBE<SliderControllerBlockEntity>, IWrenchable
{
    public static String ID = "slider";

    public SliderControllerBlock(Properties p_52591_) {
        super(p_52591_);
    }

    protected void displayScreen(SliderControllerBlockEntity entity, Player player){

        double t = entity.getControllerInfoHolder().getTarget();
        double v = entity.getControllerInfoHolder().getValue();
        PID pidParams = entity.getControllerInfoHolder().getPIDParams();

        AllPackets.sendToPlayer(
                new PIDControllerOpenScreenPacket(pidParams, v, t, entity.getBlockPos(), PIDControllerType.SLIDER),
                ((ServerPlayer)player)
        );

    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving)  {
        if(worldIn.isClientSide)return;
        withBlockEntityDo(worldIn, pos, be -> be.getExposedField().apply(worldIn.getBestNeighborSignal(pos)));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(worldIn.isClientSide)return InteractionResult.PASS;
        if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && player.isShiftKeyDown()) {
            withBlockEntityDo(worldIn, pos, SliderControllerBlockEntity::assemble);
        }else if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()){
            withBlockEntityDo(worldIn, pos, be -> this.displayScreen(be, player));
        }
        return InteractionResult.PASS;
    }



    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public Class<SliderControllerBlockEntity> getBlockEntityClass() {
        return SliderControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SliderControllerBlockEntity> getBlockEntityType() {
        return AllBlockEntities.SLIDER_CONTROLLER_BLOCKENTITY.get();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(FACING).getOpposite() == face;
    }
}
