package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.blocks.transmitter.TransmitterBlockEntity;
import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class TransmitterPeripheral extends AbstractAttachedPeripheral<TransmitterBlockEntity> {


    public TransmitterPeripheral(TransmitterBlockEntity transmitterBlockEntity) {
        super(transmitterBlockEntity);
    }


    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (!(iPeripheral instanceof TransmitterPeripheral p))return false;
        return getTarget().getBlockPos() == p.getTarget().getBlockPos();
    }


    @Override
    public String getType() {
        return "transmitter";
    }


    @LuaFunction
    public final MethodResult callRemote(IComputerAccess access, ILuaContext context, IArguments arguments) throws LuaException {
        String remoteName = arguments.getString(0);
        String methodName = arguments.getString(1);
        return getTarget().callRemote(access, context, remoteName, methodName, arguments.drop(2));
    }

    @LuaFunction
    public final MethodResult callRemoteAsync(IComputerAccess access, ILuaContext context, IArguments arguments) throws LuaException {
        String remoteName = arguments.getString(0);
        String methodName = arguments.getString(1);
        return getTarget().callRemoteAsync(access, context, remoteName, methodName, arguments.drop(2));
    }

    @LuaFunction
    public void setProtocol(long p){
        getTarget().setProtocol(p);
    }

}
