package com.verr1.controlcraft.content.cctweaked.peripheral;

import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

public class SpatialAnchorPeripheral extends AbstractAttachedPeripheral<SpatialAnchorBlockEntity>{
    public SpatialAnchorPeripheral(SpatialAnchorBlockEntity target) {
        super(target);
    }

    @Override
    public String getType() {
        return "spatial";
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof SpatialAnchorPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }


    @LuaFunction
    public final void setStatic(boolean isStatic){
        getTarget().setStatic(isStatic);
    }

    @LuaFunction
    public final void setRunning(boolean isRunning){
        getTarget().setRunning(isRunning);
    }

    @LuaFunction
    public final void setOffset(double offset){
        getTarget().setAnchorOffset(offset);
    }

    @LuaFunction
    public final void setPPID(double p, double i, double d){
        getTarget().getSchedule().setPp(p).setIp(i).setDp(d);
    }

    @LuaFunction
    public final void setQPID(double p, double i, double d){
        getTarget().getSchedule().setPq(p).setIq(i).setDq(d);
    }

    @LuaFunction
    public final void setChannel(long channel){
        getTarget().setProtocol(channel);
    }

}