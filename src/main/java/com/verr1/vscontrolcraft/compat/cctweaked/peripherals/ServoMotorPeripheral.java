package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.utils.CCUtils;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3dc;

import java.util.List;
import java.util.Map;

public class ServoMotorPeripheral extends AbstractAttachedPeripheral<AbstractServoMotor> {


    public ServoMotorPeripheral(AbstractServoMotor servoMotorBlockEntity) {
        super(servoMotorBlockEntity);
    }

    @Override
    public String getType() {
        return "servo";
    }



    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof ServoMotorPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }

    @LuaFunction
    public void setPID(double p, double i, double d){
        getTarget().getControllerInfoHolder().setParameter(p, i, d);
    }

    @LuaFunction
    public void setTargetAngle(double angle){
        getTarget().getControllerInfoHolder().setTarget(angle);
    }

    @LuaFunction
    public final double getTargetAngle(){
        return getTarget().getControllerInfoHolder().getTarget();
    }

    @LuaFunction
    public final Map<String, Map<String, Object>> getPhysics(){
        Map<String, Object> own = getTarget().ownPhysics.read().getCCPhysics();
        Map<String, Object> asm = getTarget().asmPhysics.read().getCCPhysics();
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
        Matrix3dc own = getTarget().ownPhysics.read().rotationMatrix();
        Matrix3dc asm = getTarget().asmPhysics.read().rotationMatrix();
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

}
