package com.verr1.vscontrolcraft.blocks.jointMotor;

import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.base.Servo.ICanBruteDirectionalConnect;
import com.verr1.vscontrolcraft.base.Servo.ServoMotorSyncAnimationPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.joml.*;

import java.util.List;

import static net.minecraft.ChatFormatting.GRAY;

public class JointMotorBlockEntity extends AbstractServoMotor
{
    private boolean assembleNextTick = false;

    private final LerpedFloat animatedLerpedAngle = LerpedFloat.angular();
    private float animatedAngle = 0;

    public JointMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        lazyTickRate = 0;
    }


    public Direction getServoDirection(){

        Direction facing = getBlockState().getValue(JointMotorBlock.FACING);
        Boolean align = getBlockState().getValue(JointMotorBlock.AXIS_ALONG_FIRST_COORDINATE);
        if(facing.getAxis() != Direction.Axis.X){
            if(align)return Direction.EAST;
            return facing.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        }
        if(align)return Direction.UP;
        return Direction.SOUTH;
    }

    public Vector3d getServoDirectionJOML(){
        return Util.Vec3itoVector3d(getServoDirection().getNormal());
    }

    @Override
    public BlockPos getAssembleBlockPos() {
        return getBlockPos().relative(getFacingDirection(), 1);
    }

    @Override
    public Vector3d getAssembleBlockPosJOML() {
        Vector3d p = Util.Vec3toVector3d(getBlockPos().getCenter());
        Vector3d dir = getFacingDirectionJOML();
        return p.fma(1.0, dir);
    }

    public Direction getFacingDirection(){
        return getBlockState().getValue(JointMotorBlock.FACING);
    }

    public Vector3d getFacingDirectionJOML(){
        return Util.Vec3itoVector3d(getFacingDirection().getNormal());
    }

    public void setAssembleNextTick(){
        assembleNextTick = true;
    }

    @Override
    public void tick() {
        super.tick();
        if(assembleNextTick){
            assemble();
            assembleNextTick = false;
        }

        syncCompanionAttachInducer();

    }




    public void tickAnimation(){
        animatedLerpedAngle.chase(animatedAngle, 0.5, LerpedFloat.Chaser.EXP);
        animatedLerpedAngle.tickChaser();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if(level.isClientSide)return;
        syncClient();
    }


    public void syncClient(){
        if(!level.isClientSide){
            var p = new ServoMotorSyncAnimationPacket(getBlockPos(), getServoAngle());
            AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    public void setAnimatedAngle(double angle) {
        animatedAngle = (float)angle;
    }

    public float getAnimatedAngle(float partialTick) {
        return animatedLerpedAngle.getValue(partialTick);
    }


}
