package com.verr1.controlcraft.content.blocks.loader;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.verr1.controlcraft.Config;
import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.foundation.managers.ChunkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkLoaderBlockEntity extends OnShipBlockEntity {

    public  static final ConcurrentHashMap<WorldChunkPos, Integer> commonLevelChunkHolders = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Integer> selfChunkDisclaimTicks = new ConcurrentHashMap<>();

    private int RADIUS = 2;

    public ChunkLoaderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        RADIUS = Config.ChunkLoaderRadius;

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

        for(int i = -RADIUS; i <= RADIUS; ++i) {
            for(int j = -RADIUS; j <= RADIUS; ++j) {
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
