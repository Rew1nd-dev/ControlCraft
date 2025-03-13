package com.verr1.vscontrolcraft.blocks.chunkLoader;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.verr1.vscontrolcraft.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkLoaderBlockEntity extends SmartBlockEntity {

    public  static final ConcurrentHashMap<ChunkLevelPos, Integer> commonLevelChunkHolders = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Integer> selfChunkDisclaimTicks = new ConcurrentHashMap<>();

    private int RADIUS = 2;

    public ChunkLoaderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        RADIUS = Config.ChunkLoaderRadius;
    }

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide) return;
        tickClaimedChunks();
        ServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) level, getBlockPos());
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

                claimChunk((ServerLevel) level, resetChunkPos);
            }
        }



    }

    public void disclaimedAllChunks(){
        selfChunkDisclaimTicks.forEach(
                (chunkPosLong, ticks) ->
                ChunkManager.disclaimChunk(getBlockPos(), new ChunkLevelPos((ServerLevel) level, chunkPosLong))
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
                            {
                                ChunkManager.disclaimChunk(getBlockPos(), new ChunkLevelPos((ServerLevel) level, entry.getKey()));
                            });

        selfChunkDisclaimTicks
                .entrySet()
                .removeIf(entry -> entry.getValue() <= 0);
    }


    public void claimChunk(ServerLevel serverLevel, ChunkPos chunkPos){
        ChunkManager.claimChunk(getBlockPos(), new ChunkLevelPos((ServerLevel) level, chunkPos.toLong()));
        selfChunkDisclaimTicks.put(chunkPos.toLong(), 25);
    }

    @Override
    public void destroy(){
        disclaimedAllChunks();
        super.destroy();
    }



    @Override
    public void addBehaviours(List<BlockEntityBehaviour> list) {

    }
}
