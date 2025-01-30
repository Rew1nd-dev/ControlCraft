package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.blocks.jet.JetBlockEntity;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

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
    public void setOutputThrust(double thrust){
        getTarget().thrust.write(thrust);
    }

    @LuaFunction
    public void setHorizontalTilt(double angle){
        getTarget().horizontalAngle.write(angle);
    }

    @LuaFunction
    public void setVerticalTilt(double angle){
        getTarget().verticalAngle.write(angle);
    }

}
