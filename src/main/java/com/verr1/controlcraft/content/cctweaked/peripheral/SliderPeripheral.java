package com.verr1.controlcraft.content.cctweaked.peripheral;

import com.verr1.controlcraft.content.blocks.slider.SliderBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SliderPeripheral extends AbstractAttachedPeripheral<SliderBlockEntity> {


    public SliderPeripheral(SliderBlockEntity slider) {
        super(slider);
    }

    @Override
    public String getType() {
        return "slider";
    }



    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof SliderPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }

    @LuaFunction
    public final void setOutputForce(double scale){
        getTarget().setOutputForce(scale);
    }

    @LuaFunction
    public final void setPID(double p, double i, double d){
        getTarget().getController().setParameter(p, i, d);
    }

    @LuaFunction
    public final double getDistance(){
        return getTarget().getSlideDistance();
    }

    @LuaFunction
    public final double getCurrentValue(){
        return getTarget().getController().getValue();
    }

    @LuaFunction
    public final double getTargetValue(){
        return getTarget().getController().getTarget();
    }

    @LuaFunction
    public final void setTargetValue(double target){
        getTarget().getController().setTarget(target);
    }




    @LuaFunction
    public final Map<String, Map<String, Object>> getPhysics(){
        Map<String, Object> own = getTarget().readSelf().getCCPhysics();
        Map<String, Object> asm = getTarget().readComp().getCCPhysics();
        return Map.of(
                "slider", own,
                "companion", asm
        );
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
