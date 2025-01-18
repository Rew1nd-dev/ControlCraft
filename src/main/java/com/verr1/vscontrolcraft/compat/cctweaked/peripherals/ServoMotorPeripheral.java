package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.blocks.servoMotor.ServoMotorBlockEntity;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Matrix3dc;

import java.util.Map;
import java.util.Set;

public class ServoMotorPeripheral implements IPeripheral {
    private final ServoMotorBlockEntity servoMotorBlockEntity;
    private final Set<IComputerAccess> computers = Sets.newConcurrentHashSet();

    public ServoMotorPeripheral(ServoMotorBlockEntity servoMotorBlockEntity) {
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
        return servoMotorBlockEntity.getBlockPos() == ((ServoMotorBlockEntity) servoMotorPeripheral.getTarget()).getBlockPos();
    }

    @LuaFunction
    public void setPID(double p, double i, double d){
        servoMotorBlockEntity.getControllerInfoHolder().setParameter(p, i, d);
    }

    @LuaFunction
    public void setTargetAngle(double angle){
        servoMotorBlockEntity.getControllerInfoHolder().setTargetAngle(angle);
    }

    @LuaFunction
    public Map<String, Map<String, Object>> getPhysics(){
        Map<String, Object> own = servoMotorBlockEntity.readOwnPhysicsShipInfo().getCCPhysics();
        Map<String, Object> asm = servoMotorBlockEntity.readAsmPhysicsShipInfo().getCCPhysics();
        return Map.of(
                "servo", own,
                "assem", asm
        );
    }

    @LuaFunction
    public double getAngle(){
        return servoMotorBlockEntity.getServoAngle();
    }

    @LuaFunction
    public Matrix3d getRelative(){
        Matrix3dc own = servoMotorBlockEntity.readOwnPhysicsShipInfo().rotationMatrix();
        Matrix3dc asm = servoMotorBlockEntity.readAsmPhysicsShipInfo().rotationMatrix();
        return VSMathUtils.get_xc2yc(own, asm);
    }

    @LuaFunction
    public void applyTorque(double scale){
        servoMotorBlockEntity.applyTorque(scale);
    }


}
