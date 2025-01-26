package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.blocks.slider.SliderControllerBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class SliderControllerPeripheral implements IPeripheral {
    private final SliderControllerBlockEntity sliderControllerBlockEntity;
    private final Set<IComputerAccess> computers = Sets.newConcurrentHashSet();

    public SliderControllerPeripheral(SliderControllerBlockEntity slider) {
        this.sliderControllerBlockEntity = slider;
    }

    @Override
    public String getType() {
        return "slider";
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
    }

    @Override
    public Object getTarget(){
        return sliderControllerBlockEntity;
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (iPeripheral == null)return false;
        if (!(iPeripheral instanceof ServoMotorPeripheral sliderPeripheral))return false;
        if (sliderPeripheral.getTarget() == null)return false;
        return sliderControllerBlockEntity.getBlockPos() == ((SliderControllerBlockEntity) sliderPeripheral.getTarget()).getBlockPos();
    }

    @LuaFunction
    public void setOutputForce(double scale){
        sliderControllerBlockEntity.setOutputForce(scale);
    }

    @LuaFunction
    public void setPID(double p, double i, double d){
        sliderControllerBlockEntity.getControllerInfoHolder().setParameter(p, i, d);
    }

    @LuaFunction
    public double getDistance(){
        return sliderControllerBlockEntity.getSlideDistance();
    }

    @LuaFunction
    public void setTargetDistance(double target){
        sliderControllerBlockEntity.getControllerInfoHolder().setTarget(target);
    }

    @LuaFunction
    public Map<String, Map<String, Object>> getPhysics(){
        Map<String, Object> own = sliderControllerBlockEntity.ownPhysics.read().getCCPhysics();
        Map<String, Object> asm = sliderControllerBlockEntity.cmpPhysics.read().getCCPhysics();
        return Map.of(
                "slider", own,
                "companion", asm
        );
    }

}
