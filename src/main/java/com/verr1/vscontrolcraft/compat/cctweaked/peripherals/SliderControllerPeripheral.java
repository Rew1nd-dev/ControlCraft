package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.base.Servo.AbstractServoMotor;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetBlockEntity;
import com.verr1.vscontrolcraft.blocks.slider.SliderControllerBlock;
import com.verr1.vscontrolcraft.blocks.slider.SliderControllerBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class SliderControllerPeripheral extends AbstractAttachedPeripheral<SliderControllerBlockEntity> {


    public SliderControllerPeripheral(SliderControllerBlockEntity slider) {
        super(slider);
    }

    @Override
    public String getType() {
        return "slider";
    }



    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof SliderControllerPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }

    @LuaFunction
    public final void setOutputForce(double scale){
        getTarget().setOutputForce(scale);
    }

    @LuaFunction
    public final void setPID(double p, double i, double d){
        getTarget().getControllerInfoHolder().setParameter(p, i, d);
    }

    @LuaFunction
    public final double getDistance(){
        return getTarget().getSlideDistance();
    }

    @LuaFunction
    public final void setTargetDistance(double target){
        getTarget().getControllerInfoHolder().setTarget(target);
    }

    @LuaFunction
    public final Map<String, Map<String, Object>> getPhysics(){
        Map<String, Object> own = getTarget().ownPhysics.read().getCCPhysics();
        Map<String, Object> asm = getTarget().cmpPhysics.read().getCCPhysics();
        return Map.of(
                "slider", own,
                "companion", asm
        );
    }

}
