package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
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

public class ServoMotorPeripheral implements IPeripheral {
    private final AbstractServoMotor servoMotorBlockEntity;
    private final Set<IComputerAccess> computers = Sets.newConcurrentHashSet();

    public ServoMotorPeripheral(AbstractServoMotor servoMotorBlockEntity) {
        this.servoMotorBlockEntity = servoMotorBlockEntity;
    }

    @Override
    public String getType() {
        return "servo";
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
    }

    @Override
    public Object getTarget(){
        return servoMotorBlockEntity;
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (iPeripheral == null)return false;
        if (!(iPeripheral instanceof ServoMotorPeripheral servoMotorPeripheral))return false;
        return servoMotorBlockEntity.getBlockPos() == ((AbstractServoMotor) servoMotorPeripheral.getTarget()).getBlockPos();
    }

    @LuaFunction
    public void setPID(double p, double i, double d){
        servoMotorBlockEntity.getControllerInfoHolder().setParameter(p, d, i);
    }

    @LuaFunction
    public void setTargetAngle(double angle){
        servoMotorBlockEntity.getControllerInfoHolder().setTarget(angle);
    }

    @LuaFunction
    public Map<String, Map<String, Object>> getPhysics(){
        Map<String, Object> own = servoMotorBlockEntity.ownPhysics.read().getCCPhysics();
        Map<String, Object> asm = servoMotorBlockEntity.asmPhysics.read().getCCPhysics();
        return Map.of(
                "servomotor", own,
                "companion", asm
        );
    }

    @LuaFunction
    public double getAngle(){
        return servoMotorBlockEntity.getServoAngle();
    }


    @LuaFunction
    public List<List<Double>> getRelative(){
        Matrix3dc own = servoMotorBlockEntity.ownPhysics.read().rotationMatrix();
        Matrix3dc asm = servoMotorBlockEntity.asmPhysics.read().rotationMatrix();
        return CCUtils.dumpMat3(VSMathUtils.get_xc2yc(own, asm));
    }

    @LuaFunction
    public void setOutputTorque(double scale){
        servoMotorBlockEntity.setOutputTorque(scale);
    }


}
