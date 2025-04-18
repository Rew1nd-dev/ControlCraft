package com.verr1.controlcraft.content.blocks.transmitter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.verr1.controlcraft.content.blocks.receiver.PeripheralInterfaceBlockEntity;
import com.verr1.controlcraft.foundation.data.PeripheralKey;
import com.verr1.controlcraft.content.cctweaked.peripheral.TransmitterPeripheral;
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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PeripheralProxyBlockEntity extends SmartBlockEntity {

    private long currentProtocol;

    private TransmitterPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;


    private final LoadingCache<BlockPos, Optional<PeripheralInterfaceBlockEntity>> cache = CacheBuilder.newBuilder()
                    .maximumSize(20)
                    .expireAfterWrite(1, TimeUnit.SECONDS)
                    .build(
                            new CacheLoader<>() {
                                @Override
                                public @NotNull Optional<PeripheralInterfaceBlockEntity> load(@NotNull BlockPos pos) throws Exception {
                                    return Optional.ofNullable(getLevel())
                                            .map(level -> level.getExistingBlockEntity(pos))
                                            .filter(te -> te instanceof PeripheralInterfaceBlockEntity)
                                            .map(te -> (PeripheralInterfaceBlockEntity) te);
                                }
                            });

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

    public PeripheralProxyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public MethodResult callRemote(
            IComputerAccess access,
            ILuaContext context,
            String peripheralName,
            String methodName,
            IArguments args) throws LuaException, ExecutionException {

        BlockPos peripheralPos = NetworkManager.getRegisteredPeripheralPos(new PeripheralKey(peripheralName, currentProtocol));
        if(peripheralPos == null)return MethodResult.of(null, "Receiver Not Registered");
        if(getLevel() == null)return MethodResult.of(null, "Level Is Null");
        PeripheralInterfaceBlockEntity receiver = cache.get(peripheralPos).orElse(null);
        if(receiver == null)return MethodResult.of(null, "Peripheral Is Not A Receiver");
        return receiver
                    .callPeripheral(
                            access,
                            context,
                            methodName,
                            args
                    );
    }

    public MethodResult callRemoteAsync(IComputerAccess access,
                                        ILuaContext context,
                                        String peripheralName,
                                        String methodName,
                                        IArguments args)
            throws LuaException
    {
        BlockPos peripheralPos = NetworkManager.getRegisteredPeripheralPos(new PeripheralKey(peripheralName, currentProtocol));
        if(peripheralPos == null)return MethodResult.of(null, "Receiver Not Registered");
        if(getLevel() == null)return MethodResult.of(null, "Level Is Null");
        BlockEntity receiver = getLevel().getExistingBlockEntity(peripheralPos);
        if(!(receiver instanceof PeripheralInterfaceBlockEntity))return MethodResult.of(null, "Peripheral Is Not A Receiver");
        return ((PeripheralInterfaceBlockEntity)receiver)
                .callPeripheralAsync(
                        access,
                        context,
                        methodName,
                        args
                );
    }

    @Override
    public void tick(){
        if(level.isClientSide)return;
    }

    @Override
    public void invalidate(){
        super.invalidate();
        if(peripheralCap != null){
            peripheralCap.invalidate();
            peripheralCap = null;
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }
}
