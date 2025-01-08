package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class PropellerControllerPeripheral implements IPeripheral {
    private final PropellerControllerBlockEntity controllerBlockEntity;
    private final Set<IComputerAccess> computers = Sets.newConcurrentHashSet();

    public PropellerControllerPeripheral(PropellerControllerBlockEntity controllerBlockEntity) {
        this.controllerBlockEntity = controllerBlockEntity;
    }

    @Override
    public String getType() {
        return "PropellerController";
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
        IPeripheral.super.attach(computer);
    }

    @Override
    public Object getTarget(){
        return controllerBlockEntity;
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (iPeripheral instanceof PropellerControllerPeripheral)return false;
        if (iPeripheral == null)return false;
        return controllerBlockEntity == iPeripheral.getTarget();
    }

    @LuaFunction
    public void setTargetSpeed(double speed){
        controllerBlockEntity.setTargetSpeed(speed);
    }

    @LuaFunction
    public double getTargetSpeed(){
        return controllerBlockEntity.getTargetSpeed();
    }

}
