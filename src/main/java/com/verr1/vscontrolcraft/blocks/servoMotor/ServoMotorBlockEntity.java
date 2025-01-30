package com.verr1.vscontrolcraft.blocks.servoMotor;

import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.base.Servo.ServoMotorSyncAnimationPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.joml.*;

public class ServoMotorBlockEntity extends AbstractServoMotor{

    private boolean assembleNextTick = false;

    private final LerpedFloat animatedLerpedAngle = LerpedFloat.angular();
    private float animatedAngle = 0;


    public ServoMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        lazyTickRate = 0;
    }



    public Vector3d getServoDirectionJOML(){
        return Util.Vec3itoVector3d(getBlockState().getValue(ServoMotorBlock.FACING).getNormal()) ;
    }

    @Override
    public BlockPos getAssembleBlockPos() {
        return getBlockPos().relative(getBlockState().getValue(ServoMotorBlock.FACING));
    }

    @Override
    public Vector3d getAssembleBlockPosJOML() {
        Vector3d center = Util.Vec3toVector3d(getAssembleBlockPos().getCenter());
        Vector3d dir = getServoDirectionJOML();
        return center.fma(0.0, dir);
    }

    public Direction getServoDirection(){
        return getBlockState().getValue(ServoMotorBlock.FACING);
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
        if(level.isClientSide){
            tickAnimation();
        }
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

    @Override
    public float getAnimatedAngle(float partialTick) {
        return animatedLerpedAngle.getValue(partialTick);
    }


}
