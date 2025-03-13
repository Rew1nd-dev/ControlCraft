package com.verr1.vscontrolcraft.base.ChunkLoading;

import com.verr1.vscontrolcraft.ControlCraft;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;

import java.util.function.Consumer;


// Taken and modified from SecurityCraft
// https://github.com/Geforce132/SecurityCraft/tree/1.20
public class CameraClientChunkCacheExtension {
    private static final Long2ObjectOpenHashMap<LevelChunk> CHUNK_MAP = new Long2ObjectOpenHashMap<>();
    private static final Long2ObjectOpenHashMap<LevelChunk> CHUNK_MAP_OTHER_THREADS = new Long2ObjectOpenHashMap<>();

    private CameraClientChunkCacheExtension() {}

    public static void drop(ClientLevel level, ChunkPos chunkPos) {
        if (Minecraft.getInstance().isSameThread()) {
            long chunkPosLong = chunkPos.toLong();
            LevelChunk chunk = CHUNK_MAP.get(chunkPosLong);

            if (chunk != null) {
                ControlCraft.LOGGER.info("dropped: " + chunk.getPos().x + " " + chunk.getPos().z);
                modifyChunkMaps(map -> map.remove(chunkPosLong));
                MinecraftForge.EVENT_BUS.post(new ChunkEvent.Unload(chunk));
                level.unload(chunk);

            }
        }
    }

    public static LevelChunk getChunk(int x, int z) {
        long chunkPos = ChunkPos.asLong(x, z);

        if (Minecraft.getInstance().isSameThread())
            return CHUNK_MAP.get(chunkPos);
        else {
            synchronized (CHUNK_MAP_OTHER_THREADS) {
                return CHUNK_MAP_OTHER_THREADS.get(chunkPos);
            }
        }
    }

    public static LevelChunk replaceWithPacketData(ClientLevel level, int x, int z, FriendlyByteBuf packetData, CompoundTag chunkTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> tagOutput) {
        if (!Minecraft.getInstance().isSameThread())
            throw new UnsupportedOperationException("replaceWithPacketData called off-thread, this shouldn't happen!");

        ChunkPos chunkPos = new ChunkPos(x, z);
        long longChunkPos = chunkPos.toLong();
        LevelChunk chunk = CHUNK_MAP.get(longChunkPos);

        if (chunk == null) {
            LevelChunk newChunk = new LevelChunk(level, chunkPos);

            chunk = newChunk;
            chunk.replaceWithPacketData(packetData, chunkTag, tagOutput);
            modifyChunkMaps(map -> map.put(longChunkPos, newChunk));
        }

        level.onChunkLoaded(chunkPos);


        MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk, false));
        return chunk;
    }

    private static void modifyChunkMaps(Consumer<Long2ObjectOpenHashMap<LevelChunk>> operation) {
        operation.accept(CHUNK_MAP);

        synchronized (CHUNK_MAP_OTHER_THREADS) {
            operation.accept(CHUNK_MAP_OTHER_THREADS);
        }
    }

    public static void clear() {
        CHUNK_MAP.clear();
        CHUNK_MAP_OTHER_THREADS.clear();
    }
}
