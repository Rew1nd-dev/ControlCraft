package com.verr1.vscontrolcraft.blocks.jointMotor;

import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.*;

public class JointMotorBlockEntity extends AbstractServoMotor
{
    private boolean assembleNextTick = false;


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
