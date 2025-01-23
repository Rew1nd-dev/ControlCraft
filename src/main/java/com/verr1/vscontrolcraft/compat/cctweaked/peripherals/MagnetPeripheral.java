package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetBlockEntity;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class MagnetPeripheral implements IPeripheral {
    private final MagnetBlockEntity magnetBlockEntity;
    private final Set<IComputerAccess> computers = Sets.newConcurrentHashSet();

    public MagnetPeripheral( MagnetBlockEntity magnetBlockEntity) {
        this.magnetBlockEntity = magnetBlockEntity;
    }

    @Override
    public String getType() {
        return "magnet";
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
        IPeripheral.super.attach(computer);
    }

    @Override
    public Object getTarget(){
        return magnetBlockEntity;
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof MagnetBlockEntity))return false;
        return magnetBlockEntity == iPeripheral.getTarget();
    }

    @LuaFunction
    public double getStrength(){
        return magnetBlockEntity.getStrength();
    }

    @LuaFunction
    public void setStrength(double strength){
        magnetBlockEntity.setStrength(strength);
    }

}
