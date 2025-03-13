package com.verr1.controlcraft.content.cctweaked.peripheral;

import com.verr1.controlcraft.content.blocks.motor.AbstractMotorBlockEntity;
import com.verr1.controlcraft.utils.CCUtils;
import com.verr1.controlcraft.utils.VSMathUtils;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3dc;

import java.util.List;
import java.util.Map;

public class MotorPeripheral extends AbstractAttachedPeripheral<AbstractMotorBlockEntity> {


    public MotorPeripheral(AbstractMotorBlockEntity servoMotorBlockEntity) {
        super(servoMotorBlockEntity);
    }

    @Override
    public String getType() {
        return "servo";
    }



    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof MotorPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }

    @LuaFunction
    public void setPID(double p, double i, double d){
        getTarget().getController().setParameter(p, i, d);
    }

    @LuaFunction
    public void setTargetAngle(double angle){
        getTarget().getController().setTarget(angle);
    }

    @LuaFunction
    public final double getTargetAngle(){
        return getTarget().getController().getTarget();
    }

    @LuaFunction
    public final Map<String, Map<String, Object>> getPhysics(){
        Map<String, Object> own = getTarget().readSelf().getCCPhysics();
        Map<String, Object> asm = getTarget().readComp().getCCPhysics();
        return Map.of(
                "servomotor", own,
                "companion", asm
        );
    }

    @LuaFunction
    public final double getAngle(){
        return getTarget().getServoAngle();
    }

    @LuaFunction
    public final double getCurrentValue(){
        return getTarget().getController().getValue();
    }


    @LuaFunction
    public final List<List<Double>> getRelative(){
        Matrix3dc own = getTarget().readSelf().rotationMatrix();
        Matrix3dc asm = getTarget().readComp().rotationMatrix();
        return CCUtils.dumpMat3(VSMathUtils.get_yc2xc(own, asm));
    }

    @LuaFunction
    public final void setOutputTorque(double scale){
        getTarget().setOutputTorque(scale);
    }


    @LuaFunction
    public final void setMode(boolean isAdjustingAngle){
        getTarget().setMode(isAdjustingAngle);
    }

    @LuaFunction
    public final void lock(){
        getTarget().tryLock();
    }

    @LuaFunction
    public final void unlock(){
        getTarget().tryUnlock();
    }

    @LuaFunction
    public final boolean isLocked(){
        return getTarget().isLocked();
    }

}
