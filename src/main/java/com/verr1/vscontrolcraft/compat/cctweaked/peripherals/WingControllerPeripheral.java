package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.blocks.wingController.WingControllerBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class WingControllerPeripheral extends AbstractAttachedPeripheral<WingControllerBlockEntity> {

    public WingControllerPeripheral(WingControllerBlockEntity wingControllerBlockEntity) {
        super(wingControllerBlockEntity);
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof WingControllerPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }

    @Override
    public String getType() {
        return "WingController";
    }

    @LuaFunction
    public float getAngle(){
        return getTarget().getAngle();
    }

    @LuaFunction
    public void setAngle(double angle){
        getTarget().setAngle((float)angle);
    }

}
