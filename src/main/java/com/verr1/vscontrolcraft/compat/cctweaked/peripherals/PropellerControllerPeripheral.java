package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class PropellerControllerPeripheral extends AbstractAttachedPeripheral<PropellerControllerBlockEntity> {

    public PropellerControllerPeripheral(PropellerControllerBlockEntity controllerBlockEntity) {
        super(controllerBlockEntity);
    }

    @Override
    public String getType() {
        return "PropellerController";
    }



    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof PropellerControllerPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }

    @LuaFunction
    public void setTargetSpeed(double speed){
        getTarget().setTargetSpeed(speed);
    }

    @LuaFunction
    public double getTargetSpeed(){
        return getTarget().getTargetSpeed();
    }

}
