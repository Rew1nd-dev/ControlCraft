package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.blocks.wingController.WingControllerBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class WingControllerPeripheral implements IPeripheral {
    private final WingControllerBlockEntity wingControllerBlockEntity;
    private final Set<IComputerAccess> computers = Sets.newConcurrentHashSet();

    public WingControllerPeripheral(WingControllerBlockEntity wingControllerBlockEntity) {
        this.wingControllerBlockEntity = wingControllerBlockEntity;
    }


    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
    }

    @Override
    public Object getTarget() {
        return wingControllerBlockEntity;
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (iPeripheral instanceof PropellerControllerPeripheral) return false;
        if (iPeripheral == null) return false;
        return wingControllerBlockEntity == iPeripheral.getTarget();
    }


    @Override
    public String getType() {
        return "WingController";
    }

    @LuaFunction
    public float getAngle(){
        return wingControllerBlockEntity.getAngle();
    }

    @LuaFunction
    public void setAngle(double angle){
        wingControllerBlockEntity.setAngle((float)angle);
    }

}
