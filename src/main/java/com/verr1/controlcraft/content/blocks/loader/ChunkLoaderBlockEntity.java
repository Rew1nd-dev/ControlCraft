package com.verr1.controlcraft.content.blocks.loader;

import com.verr1.controlcraft.config.BlockPropertyConfig;
import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.foundation.managers.ChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.Ship;

import java.util.concurrent.ConcurrentHashMap;

public class ChunkLoaderBlockEntity extends OnShipBlockEntity {

    public  static final ConcurrentHashMap<WorldChunkPos, Integer> commonLevelChunkHolders = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Integer> selfChunkDisclaimTicks = new ConcurrentHashMap<>();

    private int radius = 2;

    public ChunkLoaderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        radius = BlockPropertyConfig._CHUNK_LOADER_RADIUS;

    }

    public void tickClaimNew(){
        Ship ship = getShipOn();
        if(ship == null)return;
        Vector3dc shipWorldPosition = ship.getTransform().getPositionInWorld();
        // ControlCraftMod.LOGGER.info("Ship Position: " + shipWorldPosition.toString());
        BlockPos chunkBlockPos = new BlockPos(
                (int) shipWorldPosition.x(),
                (int) shipWorldPosition.y(),
                (int) shipWorldPosition.z());

        for(int i = -radius; i <= radius; ++i) {
            for(int j = -radius; j <= radius; ++j) {
                ChunkPos resetChunkPos = new ChunkPos(chunkBlockPos.offset(
                        i * 16,
                        0,
                        j * 16)
                );

                claimChunk(resetChunkPos);
            }
        }
    }

    @Override
    public void tickServer() {
        tickClaimedChunks();
        tickClaimNew();
    }

    public void disclaimedAllChunks(){
        selfChunkDisclaimTicks.forEach(
                (chunkPosLong, ticks) ->
                        ChunkManager.disclaimChunk(getBlockPos(), new WorldChunkPos(getDimensionID(), chunkPosLong))
        );
    }


    public void tickClaimedChunks(){
        selfChunkDisclaimTicks
                .forEach((chunkPosLong, ticks) -> selfChunkDisclaimTicks.put(chunkPosLong, ticks - 1));

        selfChunkDisclaimTicks
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() <= 0)
                .forEach(entry ->
                        ChunkManager.disclaimChunk(getBlockPos(), new WorldChunkPos(getDimensionID(), entry.getKey())));

        selfChunkDisclaimTicks
                .entrySet()
                .removeIf(entry -> entry.getValue() <= 0);
    }


    public void claimChunk(ChunkPos chunkPos){
        ChunkManager.claimChunk(getBlockPos(), new WorldChunkPos(getDimensionID(), chunkPos.toLong()));
        selfChunkDisclaimTicks.put(chunkPos.toLong(), 25);
    }

    @Override
    public void remove(){
        disclaimedAllChunks();
        super.destroy();
    }



}
