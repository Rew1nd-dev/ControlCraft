package com.verr1.vscontrolcraft.blocks.servoMotor;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.base.Servo.PID;
import com.verr1.vscontrolcraft.base.Servo.ServoMotorOpenScreenPacket;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.verr1.vscontrolcraft.registry.AllShapes.HALF_BOX_BASE;

public class ServoMotorBlock extends DirectionalKineticBlock implements IBE<ServoMotorBlockEntity> {
    public static String ID = "servo";

    public ServoMotorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return null;
    }

    protected void displayScreen(AbstractServoMotor entity, Player player){

        double a = entity.getControllerInfoHolder().getTargetAngle();
        PID pidParams = entity.getControllerInfoHolder().getPIDParams();

        AllPackets.sendToPlayer(
                new ServoMotorOpenScreenPacket(pidParams, a, entity.getBlockPos()),
                ((ServerPlayer)player)
        );

    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(worldIn.isClientSide)return InteractionResult.SUCCESS;
        if(AllItems.WRENCH.isIn(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            withBlockEntityDo(worldIn, pos, ServoMotorBlockEntity::setAssembleNextTick);
        }else if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()){
            withBlockEntityDo(worldIn, pos, be -> this.displayScreen(be, player));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return HALF_BOX_BASE.get(state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }


    @Override
    public Class<ServoMotorBlockEntity> getBlockEntityClass() {
        return ServoMotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ServoMotorBlockEntity> getBlockEntityType() {
        return AllBlockEntities.SERVO_MOTOR_BLOCKENTITY.get();
    }
}
