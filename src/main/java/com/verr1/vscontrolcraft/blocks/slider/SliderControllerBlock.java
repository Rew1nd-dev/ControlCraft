package com.verr1.vscontrolcraft.blocks.slider;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.base.Servo.PID;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerOpenScreenPacket;
import com.verr1.vscontrolcraft.blocks.servoMotor.ServoMotorBlockEntity;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class SliderControllerBlock extends DirectionalBlock implements IBE<SliderControllerBlockEntity> {
    public static String ID = "slider";

    public SliderControllerBlock(Properties p_52591_) {
        super(p_52591_);
    }

    protected void displayScreen(SliderControllerBlockEntity entity, Player player){

        double a = entity.getControllerInfoHolder().getTarget();
        PID pidParams = entity.getControllerInfoHolder().getPIDParams();

        AllPackets.sendToPlayer(
                new PIDControllerOpenScreenPacket(pidParams, a, entity.getBlockPos()),
                ((ServerPlayer)player)
        );

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(worldIn.isClientSide)return InteractionResult.SUCCESS;
        if(AllItems.WRENCH.isIn(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            withBlockEntityDo(worldIn, pos, SliderControllerBlockEntity::assemble);
        }else if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()){
            withBlockEntityDo(worldIn, pos, be -> this.displayScreen(be, player));
        }
        return InteractionResult.SUCCESS;
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
}
