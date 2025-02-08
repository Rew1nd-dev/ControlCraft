package com.verr1.vscontrolcraft.blocks.recevier;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.blocks.transmitter.NetworkManager;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.methods.PeripheralMethod;
import dan200.computercraft.impl.Peripherals;
import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.platform.InvalidateCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ReceiverBlockEntity extends SmartBlockEntity {

    private IPeripheral attachedPeripheral;
    private final Map<String, PeripheralMethod> methods = new HashMap<>();
    private PeripheralKey networkKey = PeripheralKey.NULL;

    private ConcurrentLinkedQueue<Runnable> syncTasks = new ConcurrentLinkedQueue<>();

    public MethodResult callPeripheral(IComputerAccess access, ILuaContext context, String methodName, IArguments args) throws LuaException {
        if(level.isClientSide)return MethodResult.of(null, "You Are Calling This On The Client Side, Nothing Returned");
        if(attachedPeripheral == null) return MethodResult.of(null, "Receiver Called, But No Peripheral Attached");
        if(!methods.containsKey(methodName))return MethodResult.of(null, "Receiver Called, But Method Not Found");
        if(access == null)return MethodResult.of(null, "Receiver Called, But No Access Provided");
        return methods.get(methodName).apply(attachedPeripheral, context, access, args);
    }

    public MethodResult callPeripheralAsync(IComputerAccess access, ILuaContext context, String methodName, IArguments args){
        if(level.isClientSide)return MethodResult.of(null, "You Are Calling This On The Client Side, Nothing Returned");
        if(attachedPeripheral == null) return MethodResult.of(null, "Receiver Called, But No Peripheral Attached");
        if(!methods.containsKey(methodName))return MethodResult.of(null, "Receiver Called, But Method Not Found");
        if(access == null)return MethodResult.of(null, "Receiver Called, But No Access Provided");
        enqueueTask(()->{
            try {
                methods.get(methodName).apply(attachedPeripheral, context, access, args);
            } catch (Exception e) {
                ControlCraft.LOGGER.info("Lua Exception Of: {}", e.getMessage());
            }
        });
        return MethodResult.of("queued");
    }

    public void enqueueTask(Runnable r){
        if(syncTasks.size() < 256) syncTasks.add(r);
    }

    public void executeAll(){
        while(!syncTasks.isEmpty()){
            syncTasks.poll().run();
        }
    }

    public String getAttachedPeripheralType(){
        if(level.isClientSide)return "You Are Calling This On The Client Side, Nothing Returned";
        if(attachedPeripheral == null)return "Not Attached";
        return attachedPeripheral.getType();
    }


    public void deleteAttachedPeripheral(){
        attachedPeripheral = null;
        methods.clear();
    }

    public void updateAttachedPeripheral(){
        if(level == null || level.isClientSide)return;
        deleteAttachedPeripheral();
        Direction attachedDirection = getBlockState().getValue(ReceiverBlock.FACING);
        BlockPos attachedPos = getBlockPos()
                .offset(
                        attachedDirection
                                .getOpposite()
                                .getNormal()
                );
        IPeripheral peripheral = Peripherals.getPeripheral(
                (ServerLevel)level,
                attachedPos,
                attachedDirection,
                new InvalidPeripheralCallBack()
        );
        attachedPeripheral = peripheral;
        if(attachedPeripheral == null)return;
        methods.putAll(ServerContext.get(((ServerLevel) level).getServer()).peripheralMethods().getSelfMethods(peripheral));
    }

    public PeripheralKey getNetworkKey(){
        if(registered())return networkKey;
        return PeripheralKey.NULL;
    }

    public boolean registered(){
        return NetworkManager.isRegistered(getBlockPos());
    }

    public void resetNetworkRegistry(PeripheralKey newKey){
        if(level == null)return;
        if(level.isClientSide)return;
        if(Objects.equals(newKey.Name(), ""))return;
        networkKey = NetworkManager.registerAndGetKey(newKey, getBlockPos());
        sendData();
    }

    @Override
    public void tick(){
        super.tick();
        if(level.isClientSide)return;
        NetworkManager.activate(getBlockPos());
        executeAll();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        updateAttachedPeripheral();
    }

    public ReceiverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void destroy(){
        super.destroy();
        if(level.isClientSide)return;
        NetworkManager.UnregisterWirelessPeripheral(networkKey);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if(!clientPacket){
            tag.putLong("protocol", networkKey.Protocol());
            tag.putString("name", networkKey.Name());
        }

    }


    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if(!clientPacket){
            long protocol = tag.getLong("protocol");
            String name = tag.getString("name");
            syncTasks.add(()-> resetNetworkRegistry(new PeripheralKey(name, protocol)));
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    private class InvalidPeripheralCallBack implements InvalidateCallback{
        @Override
        public void run() {
            deleteAttachedPeripheral();
        }
    }
}
