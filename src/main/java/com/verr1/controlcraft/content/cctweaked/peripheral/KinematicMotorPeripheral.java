package com.verr1.controlcraft.content.cctweaked.peripheral;

import com.verr1.controlcraft.content.blocks.motor.AbstractKinematicMotor;
import com.verr1.controlcraft.utils.CCUtils;
import com.verr1.controlcraft.utils.VSMathUtils;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3dc;

import java.util.List;
import java.util.Map;

public class KinematicMotorPeripheral extends AbstractAttachedPeripheral<AbstractKinematicMotor> {


    public KinematicMotorPeripheral(AbstractKinematicMotor servoMotorBlockEntity) {
        super(servoMotorBlockEntity);
    }

    @Override
    public String getType() {
        return "servo";
    }



    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof DynamicMotorPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
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
    public final double getControlTarget(){
        return getTarget().getController().getControlTarget();
    }

    @LuaFunction
    public final void setControlTarget(double target){
        getTarget().getController().setControlTarget(target);
    }



    @LuaFunction
    public final Map<String, Map<String, Object>> getPhysics(){
        Map<String, Object> own = getTarget().readSelf().toLua();
        Map<String, Object> asm = getTarget().readComp().toLua();
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
    public final List<List<Double>> getRelative(){
        Matrix3dc own = getTarget().readSelf().rotationMatrix();
        Matrix3dc asm = getTarget().readComp().rotationMatrix();
        return CCUtils.dumpMat3(VSMathUtils.get_yc2xc(own, asm));
    }


    @LuaFunction
    public final void setIsForcingAngle(boolean isAdjustingAngle){
        getTarget().setMode(isAdjustingAngle);
    }


}
