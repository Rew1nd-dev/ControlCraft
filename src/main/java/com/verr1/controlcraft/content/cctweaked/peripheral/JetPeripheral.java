package com.verr1.controlcraft.content.cctweaked.peripheral;

import com.verr1.controlcraft.content.blocks.jet.JetBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

public class JetPeripheral extends AbstractAttachedPeripheral<JetBlockEntity> {

    public JetPeripheral(JetBlockEntity jet) {
        super(jet);
    }

    @Override
    public String getType() {
        return "jet";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof JetPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }

    @LuaFunction
    public final void setOutputThrust(double thrust){
        getTarget().thrust.write(thrust);
    }

    @LuaFunction
    public final void setHorizontalTilt(double angle){
        getTarget().horizontalAngle.write(angle);
    }

    @LuaFunction
    public final void setVerticalTilt(double angle){
        getTarget().verticalAngle.write(angle);
    }

}
