package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.blocks.servoMotor.ServoMotorBlockEntity;
import com.verr1.vscontrolcraft.utils.CCUtils;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3dc;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
        getTarget().getControllerInfoHolder().setParameter(p, d, i);
    }

    @LuaFunction
    public void setTargetAngle(double angle){
        getTarget().getControllerInfoHolder().setTarget(angle);
    }

    @LuaFunction
    public double getTargetAngle(){
        return getTarget().getControllerInfoHolder().getTarget();
    }

    @LuaFunction
    public Map<String, Map<String, Object>> getPhysics(){
        Map<String, Object> own = getTarget().ownPhysics.read().getCCPhysics();
        Map<String, Object> asm = getTarget().asmPhysics.read().getCCPhysics();
        return Map.of(
                "servomotor", own,
                "companion", asm
        );
    }

    @LuaFunction
    public double getAngle(){
        return getTarget().getServoAngle();
    }


    @LuaFunction
    public List<List<Double>> getRelative(){
        Matrix3dc own = getTarget().ownPhysics.read().rotationMatrix();
        Matrix3dc asm = getTarget().asmPhysics.read().rotationMatrix();
        return CCUtils.dumpMat3(VSMathUtils.get_xc2yc(own, asm));
    }

    @LuaFunction
    public void setOutputTorque(double scale){
        getTarget().setOutputTorque(scale);
    }


}
