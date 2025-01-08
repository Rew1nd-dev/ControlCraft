package com.verr1.vscontrolcraft.compat.cctweaked.peripherals;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.blocks.transmitter.TransmitterBlockEntity;
import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class TransmitterPeripheral implements IPeripheral {

    private final TransmitterBlockEntity transmitterBlockEntity;
    private final Set<IComputerAccess> computers = Sets.newConcurrentHashSet();

    public TransmitterPeripheral(TransmitterBlockEntity transmitterBlockEntity) {
        this.transmitterBlockEntity = transmitterBlockEntity;
    }



    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
    }

    @Override
    public Object getTarget(){
        return transmitterBlockEntity;
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }

    @Override
    public boolean equals(@Nullable IPeripheral iPeripheral) {
        if (iPeripheral instanceof PropellerControllerPeripheral)return false;
        if (iPeripheral == null)return false;
        return transmitterBlockEntity == iPeripheral.getTarget();
    }


    @Override
    public String getType() {
        return "transmitter";
    }


    @LuaFunction
    public MethodResult callRemote(IComputerAccess access, ILuaContext context, IArguments arguments) throws LuaException {
        String remoteName = arguments.getString(0);
        String methodName = arguments.getString(1);
        return transmitterBlockEntity.callRemote(access, context, remoteName, methodName, arguments.drop(2));
    }

    @LuaFunction
    public void setProtocol(long p){
        transmitterBlockEntity.setProtocol(p);
    }

}
