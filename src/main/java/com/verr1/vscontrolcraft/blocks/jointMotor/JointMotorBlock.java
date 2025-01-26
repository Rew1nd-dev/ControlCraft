package com.verr1.vscontrolcraft.blocks.jointMotor;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.base.Servo.PID;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerOpenScreenPacket;
import com.verr1.vscontrolcraft.registry.AllBlockEntities;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.verr1.vscontrolcraft.registry.AllShapes.HALF_BOX_BASE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JointMotorBlock extends DirectionalAxisKineticBlock implements IBE<JointMotorBlockEntity> {
    public static final String ID = "joint";

    public JointMotorBlock(Properties p_49795_) {
        super(p_49795_);
    }


    protected void displayScreen(AbstractServoMotor entity, Player player){

        double a = entity.getControllerInfoHolder().getTarget();
        PID pidParams = entity.getControllerInfoHolder().getPIDParams();

        AllPackets.sendToPlayer(
                new PIDControllerOpenScreenPacket(pidParams, a, entity.getBlockPos()),
                ((ServerPlayer)player)
        );

    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return HALF_BOX_BASE.get(state.getValue(FACING));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit){
        if(worldIn.isClientSide)return InteractionResult.SUCCESS;
        if(AllItems.WRENCH.isIn(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            withBlockEntityDo(worldIn, pos, JointMotorBlockEntity::setAssembleNextTick);
        }else if(player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()){
            withBlockEntityDo(worldIn, pos, be -> this.displayScreen(be, player));
        }
        return InteractionResult.SUCCESS;
    }


    @Override
    public Class<JointMotorBlockEntity> getBlockEntityClass() {
        return JointMotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends JointMotorBlockEntity> getBlockEntityType() {
        return AllBlockEntities.JOINT_MOTOR_BLOCKENTITY.get();
    }
}
