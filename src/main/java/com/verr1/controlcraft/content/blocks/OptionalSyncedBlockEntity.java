package com.verr1.controlcraft.content.blocks;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.network.packets.specific.LazyRequestBlockEntitySyncPacket;
import com.verr1.controlcraft.foundation.network.packets.specific.SyncBlockEntityClientPacket;
import com.verr1.controlcraft.foundation.network.packets.specific.SyncBlockEntityServerPacket;
import com.verr1.controlcraft.foundation.type.Side;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class OptionalSyncedBlockEntity extends KineticBlockEntity {

    private final ArrayList<SerializeUtils.ReadWriter<?>> readWriter = new ArrayList<>();
    private final ArrayList<SerializeUtils.ReadWriteExecutor> readWriteExecutor = new ArrayList<>();
    private final HashMap<NetworkKey, SerializeUtils.LockableReadWriter<?>> networkReadWriter = new HashMap<>();
    private final HashMap<NetworkKey, SerializeUtils.LockableReadWriteExecutor> networkExecutor = new HashMap<>();

    public OptionalSyncedBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }


    protected void registerFieldReadWriter(SerializeUtils.ReadWriter<?> rw, Side side){
        switch (side){
            case SERVER_ONLY -> readWriter.add(rw);
            case SHARED -> {
                readWriter.add(rw);
                networkReadWriter.put(rw.getKey(), new SerializeUtils.LockableReadWriter<>(rw));
            }
            case RUNTIME_SHARED -> networkReadWriter.put(rw.getKey(), new SerializeUtils.LockableReadWriter<>(rw));
            // case CLIENT -> clientReadWriter.add(rw);
            // case COMMON -> commonReadWriter.add(rw);
        }
    }

    private void tickLock(){
        networkReadWriter.values().forEach(rw -> rw.readLock.tick());
        networkExecutor.values().forEach(e -> e.readLock.tick());
    }

    public boolean isAnyDirty(NetworkKey... key){
        AtomicBoolean isAllUpdated = new AtomicBoolean(true);
        Arrays.asList(key).forEach(
                k -> {
                    Optional
                            .ofNullable(networkReadWriter.get(k))
                            .ifPresent(rw -> isAllUpdated.set(isAllUpdated.get() & rw.readLock.isUpdated()));
                    Optional
                            .ofNullable(networkExecutor.get(k))
                            .ifPresent(e -> isAllUpdated.set(isAllUpdated.get() & e.readLock.isUpdated()));
                }
        );
        return !isAllUpdated.get();
    }

    public void setDirty(NetworkKey... key){
        Arrays.asList(key).forEach(
                k -> {
                    Optional
                            .ofNullable(networkReadWriter.get(k))
                            .ifPresent(rw -> rw.readLock.setDirty());
                    Optional
                            .ofNullable(networkExecutor.get(k))
                            .ifPresent(e -> e.readLock.setDirty());
                }
        );
    }


    public void tickServer(){

    }

    public void tickClient(){

    }



    public void tickCommon(){
        tickLock();
    }

    public void lazyTickServer(){

    }

    public void lazyTickClient(){

    }


    public void lazyTickCommon(){

    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        lazyTickCommon();
        if(level != null && level.isClientSide)lazyTickClient();
        else lazyTickServer();
    }

    public void tick(){
        super.tick();
        tickCommon();
        if(level != null && level.isClientSide)tickClient();
        else tickServer();
    }


    public void activateLock(NetworkKey... key){
        Arrays.asList(key).forEach(
                k -> {
                    Optional
                            .ofNullable(networkReadWriter.get(k))
                            .ifPresent(rw -> rw.readLock.activate());
                    Optional
                            .ofNullable(networkExecutor.get(k))
                            .ifPresent(e -> e.readLock.activate());
                }
        );
    }

    protected void registerReadWriteExecutor(SerializeUtils.ReadWriteExecutor executor, Side side){
        switch (side){
            case SERVER_ONLY -> readWriteExecutor.add(executor);
            case SHARED -> {
                readWriteExecutor.add(executor);
                networkExecutor.put(executor.getKey(), new SerializeUtils.LockableReadWriteExecutor(executor));
            }
            case RUNTIME_SHARED -> networkExecutor.put(executor.getKey(), new SerializeUtils.LockableReadWriteExecutor(executor));
        }
    }

    public void syncForPlayer(ServerPlayer player, NetworkKey... key){
        if(level == null || level.isClientSide)return;
        sync(PacketDistributor.PLAYER.with(() -> player), key);
    }

    public void syncForAllPlayers(NetworkKey... key){
        if(level == null || level.isClientSide)return;
        sync(PacketDistributor.ALL.noArg(), key);
    }

    public void request(List<NetworkKey> requests){
        if(level == null || !level.isClientSide)return;
        var p = new LazyRequestBlockEntitySyncPacket(getBlockPos(), requests);
        ControlCraftPackets.getChannel().sendToServer(p);
    }


    public void request(NetworkKey... requests){
        if(level == null || !level.isClientSide)return;
        var p = new LazyRequestBlockEntitySyncPacket(getBlockPos(), List.of(requests));
        ControlCraftPackets.getChannel().sendToServer(p);
    }

    public void receiveRequest(List<NetworkKey> requests, ServerPlayer sender){
        syncForPlayer(sender, Arrays.copyOf(requests.toArray(), requests.size(), NetworkKey[].class));
    }

    public void syncForNear(NetworkKey... key){
        if(level == null)return;
        BlockPos pos = getBlockPos();
        sync(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 64, level.dimension())), key);
    }

    public void syncToServer(NetworkKey... key){
        if(level == null || !level.isClientSide)return;
        sync(PacketDistributor.SERVER.noArg(), key);
    }

    protected void sync(PacketDistributor.PacketTarget target, NetworkKey... key){
        CompoundTag tag = new CompoundTag();
        // tag.putString("key", key);

        Arrays.asList(key).forEach(
                k -> {
                    Optional
                            .ofNullable(networkReadWriter.get(k))
                            .ifPresent(rw -> rw.writeDefault(tag));
                    Optional
                            .ofNullable(networkExecutor.get(k))
                            .ifPresent(e -> e.writeWithKey(tag));
                }
        );
        if (level != null && !level.isClientSide) {
            var p = new SyncBlockEntityClientPacket(getBlockPos(), tag);
            ControlCraftPackets.getChannel().send(target, p);
        }
        if (level != null &&  level.isClientSide) {
            var p = new SyncBlockEntityServerPacket(getBlockPos(), tag);
            ControlCraftPackets.getChannel().sendToServer(p);
        }
    }

    public void receiveSync(CompoundTag tag){
        networkReadWriter.values().forEach(rw -> rw.readAndUpdateWithKey(tag));
        networkExecutor.values().forEach(e -> e.readAndUpdateWithKey(tag));
        if(level == null || level.isClientSide)return;
        setChanged();
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);

        if(clientPacket)return;
        readWriter.forEach(rw -> rw.onReadDefault(compound));
        readWriteExecutor.forEach(e -> e.onReadDefault(compound));
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if(clientPacket)return;
        readWriter.forEach(rw -> rw.onWriteDefault(compound));
        readWriteExecutor.forEach(e -> e.onWriteDefault(compound));
    }

}
