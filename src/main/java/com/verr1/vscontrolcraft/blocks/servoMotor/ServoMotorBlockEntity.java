package com.verr1.vscontrolcraft.blocks.servoMotor;

import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.*;

import java.lang.Math;

public class ServoMotorBlockEntity extends AbstractServoMotor{

    private boolean assembleNextTick = false;


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
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        double createInput2Omega = speed / 60 * 2 * Math.PI;
        if(!isAdjustingAngle()) getControllerInfoHolder().setTarget(createInput2Omega);
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

    @Override
    public void lazyTick() {
        super.lazyTick();
        if(level.isClientSide)return;
        syncClient();
    }




}
