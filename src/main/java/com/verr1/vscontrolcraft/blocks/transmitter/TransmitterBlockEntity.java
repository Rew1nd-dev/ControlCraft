package com.verr1.vscontrolcraft.blocks.transmitter;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.verr1.vscontrolcraft.blocks.recevier.PeripheralKey;
import com.verr1.vscontrolcraft.blocks.recevier.ReceiverBlockEntity;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.TransmitterPeripheral;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TransmitterBlockEntity extends SmartBlockEntity {

    private long currentProtocol;

    private TransmitterPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new TransmitterPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public void setProtocol(long p){
        currentProtocol = p;
    }

    public TransmitterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public MethodResult callRemote(
            IComputerAccess access,
            ILuaContext context,
            String peripheralName,
            String methodName,
            IArguments args) throws LuaException
    {

        BlockPos peripheralPos = NetworkManager.getRegisteredPeripheralPos(new PeripheralKey(peripheralName, currentProtocol));
        if(peripheralPos == null)return MethodResult.of(null, "Receiver Not Registered");
        if(getLevel() == null)return MethodResult.of(null, "Level Is Null");
        BlockEntity receiver = getLevel().getExistingBlockEntity(peripheralPos);
        if(!(receiver instanceof ReceiverBlockEntity))return MethodResult.of(null, "Peripheral Is Not A Receiver");
        return ((ReceiverBlockEntity)receiver)
                    .callPeripheral(
                            access,
                            context,
                            methodName,
                            args
                    );
    }

    @Override
    public void tick(){
        if(level.isClientSide)return;
        // ControlCraft.LOGGER.info("Transmitter Tick");
    }


    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }
}
