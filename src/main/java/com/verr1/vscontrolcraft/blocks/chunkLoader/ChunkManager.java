package com.verr1.vscontrolcraft.blocks.chunkLoader;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;


import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;


public class ChunkManager {
    private static final ConcurrentHashMap<ChunkLevelPos, HashSet<BlockPos>> ServerLevelChunkHolders = new ConcurrentHashMap<>();
    private static int lazyTickRate = 0;


    public static void tick(TickEvent.ServerTickEvent event){
        // ControlCraftMod.LOGGER.info("ChunkManager.tick called " + ServerLevelChunkUnloadTicks.size());
        removeNoHolderChunks();
        lazyTick();
    }

    public static void lazyTick(){
        if(lazyTickRate-- > 0)return;
        lazyTickRate = 50;
        removeInvalidHolders();
    }

    public static void removeInvalidHolders(){
        ServerLevelChunkHolders
                .forEach(
                        (chunkLevelPos, owners) -> {
                            owners.removeIf(owner -> {
                                        ServerLevel serverLevel = chunkLevelPos.serverLevel();
                                        if(serverLevel == null) return true;
                                        if(!(serverLevel.getBlockEntity(owner) instanceof ChunkLoaderBlockEntity))return true;
                                        return false;
                                    }
                            );
                        }
                );
    }

    public static void removeNoHolderChunks(){
        ServerLevelChunkHolders
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().isEmpty())
                .forEach(
                        entry -> forceLevelChunk(
                                entry.getKey().serverLevel(),
                                entry.getKey().chunkPosLong(),
                                false
                        )
                );
        ServerLevelChunkHolders
                .entrySet()
                .removeIf(entry -> entry.getValue().isEmpty());
    }


    public static void claimChunk(BlockPos owner, ChunkLevelPos chunkLevelPos){
        ChunkManager.forceLevelChunk((ServerLevel) chunkLevelPos.serverLevel(), chunkLevelPos.chunkPosLong(), true);
        ServerLevelChunkHolders.computeIfAbsent(chunkLevelPos, k -> new HashSet<>()).add(owner);
    }

    public static void disclaimChunk(BlockPos owner, ChunkLevelPos chunkLevelPos){
        ServerLevelChunkHolders.computeIfPresent(chunkLevelPos, (k, v) -> {
            v.remove(owner);
            return v;
        });
    }


    public static void forceLevelChunk(ServerLevel serverLevel, long chunkPosLong, boolean forced){
        serverLevel.setChunkForced(
                new ChunkPos(chunkPosLong).x,
                new ChunkPos(chunkPosLong).z,
                forced
        );
    }



}
