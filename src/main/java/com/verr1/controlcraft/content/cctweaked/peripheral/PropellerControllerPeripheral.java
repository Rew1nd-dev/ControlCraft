package com.verr1.controlcraft.content.cctweaked.peripheral;

import com.verr1.controlcraft.content.blocks.propeller.PropellerControllerBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

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
    public final void setTargetSpeed(double speed){
        getTarget().setTargetSpeed(speed);
    }

    @LuaFunction
    public final double getTargetSpeed(){
        return getTarget().getTargetSpeed();
    }

}
