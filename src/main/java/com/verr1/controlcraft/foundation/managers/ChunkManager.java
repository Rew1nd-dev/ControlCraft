package com.verr1.controlcraft.foundation.managers;

import com.verr1.controlcraft.content.blocks.loader.ChunkLoaderBlockEntity;
import com.verr1.controlcraft.content.blocks.loader.WorldChunkPos;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkManager {
    private static final ConcurrentHashMap<WorldChunkPos, HashSet<BlockPos>> ServerLevelChunkHolders = new ConcurrentHashMap<>();
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
                        (chunkLevelPos, owners) ->
                                owners.removeIf(
                                        owner -> BlockEntityGetter
                                                .getLevelBlockEntityAt(
                                                        chunkLevelPos.serverLevel(),
                                                        owner,
                                                        ChunkLoaderBlockEntity.class)
                                                .isEmpty()
                        )
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


    public static void claimChunk(BlockPos owner, WorldChunkPos chunkLevelPos){
        ChunkManager.forceLevelChunk(chunkLevelPos.serverLevel(), chunkLevelPos.chunkPosLong(), true);
        ServerLevelChunkHolders.computeIfAbsent(chunkLevelPos, k -> new HashSet<>()).add(owner);
    }

    public static void disclaimChunk(BlockPos owner, WorldChunkPos chunkLevelPos){
        ServerLevelChunkHolders.computeIfPresent(chunkLevelPos, (k, v) -> {
            v.remove(owner);
            return v;
        });
    }


    public static void forceLevelChunk(@Nullable ServerLevel serverLevel, long chunkPosLong, boolean forced){
        if(serverLevel == null)return;
        serverLevel.setChunkForced(
                new ChunkPos(chunkPosLong).x,
                new ChunkPos(chunkPosLong).z,
                forced
        );
    }



}
