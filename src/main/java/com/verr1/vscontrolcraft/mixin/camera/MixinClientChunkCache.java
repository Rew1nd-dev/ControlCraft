package com.verr1.vscontrolcraft.mixin.camera;


import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.ChunkLoading.CameraClientChunkCacheExtension;
import com.verr1.vscontrolcraft.base.ChunkLoading.IChunkStorageProvider;
import com.verr1.vscontrolcraft.blocks.camera.LinkedCameraManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = ClientChunkCache.class)
abstract class MixinClientChunkCache implements IChunkStorageProvider {

    @Shadow
    volatile ClientChunkCache.Storage storage;

    @Shadow
    @Final
    ClientLevel level;


    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void securitycraft$onDrop(int x, int z, CallbackInfo ci) {


        // ControlCraft.LOGGER.info("should drop: " + x + " " + z);
        /*
        *

        if (LinkedCameraManager.isIsLinked() && pos.getChessboardDistance(new ChunkPos(LinkedCameraManager.getLinkCameraPos())) <= (renderDistance + 1))
            ci.cancel();
        *
        **/


        // CameraClientChunkCacheExtension.drop(level, pos);
    }


    @Inject(method = "replaceWithPacketData", at = @At("HEAD"), cancellable = true)
    private void securitycraft$onReplaceChunk(int x, int z, FriendlyByteBuf buffer, CompoundTag chunkTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> tagOutputConsumer, CallbackInfoReturnable<LevelChunk> cir) {
        ChunkPos playerChunkPos = Optional.ofNullable(Minecraft.getInstance().player).map(Entity::chunkPosition).orElse(ChunkPos.ZERO);
        ControlCraft.LOGGER.info("trying replace:" + x + " " + z + " Player At:" + playerChunkPos.x + " " + playerChunkPos.z);

        /*
        * int renderDistance = Minecraft.getInstance().options.renderDistance().get();
        ChunkPos pos = new ChunkPos(x, z);
        boolean isInPlayerRange = storage.inRange(x, z);
        boolean shouldAddChunk = false;

        if (LinkedCameraManager.isIsLinked() && pos.getChessboardDistance(new ChunkPos(LinkedCameraManager.getLinkCameraPos())) <= (renderDistance + 1))
            shouldAddChunk = true;


        if (shouldAddChunk) {
            LevelChunk newChunk = CameraClientChunkCacheExtension.replaceWithPacketData(level, x, z, new FriendlyByteBuf(buffer.copy()), chunkTag, tagOutputConsumer);

            if (!isInPlayerRange)
                cir.setReturnValue(newChunk);
        }
        *
        * */

    }



    /**
     * Places clientside received chunks which are in range of a mounted camera or a frame camera into the camera client chunk
     * cache
     */
    @Inject(method = "getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/LevelChunk;", at = @At("TAIL"), cancellable = true)
    private void securitycraft$onGetChunk(int x, int z, ChunkStatus requiredStatus, boolean requireChunk, CallbackInfoReturnable<LevelChunk> cir) {
        /*
        * if (!storage.inRange(x, z)) {
            LevelChunk chunk = CameraClientChunkCacheExtension.getChunk(x, z);

            if (chunk != null)
                cir.setReturnValue(chunk);
        }
        * */
    }

}
