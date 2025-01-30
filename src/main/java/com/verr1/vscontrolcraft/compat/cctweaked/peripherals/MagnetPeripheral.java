package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.blocks.jet.JetBlockEntity;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetBlockEntity;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;


public class MagnetPeripheral extends AbstractAttachedPeripheral<MagnetBlockEntity> {


    public MagnetPeripheral(MagnetBlockEntity magnetBlockEntity) {
        super(magnetBlockEntity);

    }

    @Override
    public String getType() {
        return "magnet";
    }


    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof MagnetPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }

    @LuaFunction
    public double getStrength(){
        return getTarget().getStrength();
    }

    @LuaFunction
    public void setStrength(double strength){
        getTarget().setStrength(strength);
    }

}
