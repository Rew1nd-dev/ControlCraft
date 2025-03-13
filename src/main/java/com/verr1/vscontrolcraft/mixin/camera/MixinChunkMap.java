package com.verr1.vscontrolcraft.mixin.camera;

import com.google.common.collect.ImmutableList;
import com.verr1.vscontrolcraft.base.ChunkLoading.ChunkLoaderFakePlayer;
import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.blocks.camera.ServerCameraManager;
import com.verr1.vscontrolcraft.utils.Util;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Mixin(ChunkMap.class)
abstract class MixinChunkMap {

    @Unique
    private static final Map<ServerPlayer, SectionPos> OLD_SECTION_POSITIONS = new HashMap<>();
    @Shadow
    @Final
    private PlayerMap playerMap;
    @Shadow
    int viewDistance;
    @Final
    @Shadow
    ServerLevel level;

    @Shadow
    protected abstract void updateChunkTracking(ServerPlayer player, ChunkPos chunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> packetCache, boolean wasLoaded, boolean load);

    @Shadow
    public static boolean isChunkInRange(int p_200879_, int p_200880_, int p_200881_, int p_200882_, int p_200883_) {
        return false;
    }

    @Shadow @Nullable protected abstract ChunkHolder getVisibleChunkIfPresent(long p_140328_);

    @Shadow protected abstract void playerLoadedChunk(ServerPlayer p_183761_, MutableObject<ClientboundLevelChunkWithLightPacket> p_183762_, LevelChunk p_183763_);

    @Shadow protected abstract CompletableFuture<Optional<CompoundTag>> readChunk(ChunkPos p_214964_);

    @Inject(method = "setViewDistance", at = @At("HEAD"))
    private void securitycraft$setCameraSectionPos(int viewDistance, CallbackInfo ci) {
        for (ServerPlayer player : playerMap.getPlayers(0)) { //the parameter is ignored by the game
            if (ServerCameraManager.isRegistered(player.getUUID())) {
                OLD_SECTION_POSITIONS.put(player, player.getLastSectionPos());
                player.setLastSectionPos(SectionPos.of(ServerCameraManager.getCamera(player).pos()));
            }
        }
    }

    @Inject(method = "setViewDistance", at = @At("TAIL"))
    private void securitycraft$restorePreviousSectionPos(int viewDistance, CallbackInfo ci) {
        for (Map.Entry<ServerPlayer, SectionPos> entry : OLD_SECTION_POSITIONS.entrySet()) {
            entry.getKey().setLastSectionPos(entry.getValue());
        }

        OLD_SECTION_POSITIONS.clear();
    }

    @Redirect(method = "getPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getLastSectionPos()Lnet/minecraft/core/SectionPos;"))
    private SectionPos securitycraft$getCameraSectionPos(ServerPlayer player) {
        if (ServerCameraManager.isRegistered(player.getUUID()))
            return SectionPos.of(ServerCameraManager.getCamera(player).pos());

        return player.getLastSectionPos();
    }


    @Inject(method = "updateChunkTracking", at = @At("HEAD"), cancellable = true)
    private void redirectToUser(ServerPlayer player, ChunkPos chunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> packet, boolean bl1, boolean bl2, CallbackInfo ci){
        if(!(player instanceof ChunkLoaderFakePlayer chunkLoader))return;
        ServerPlayer user = chunkLoader.getLinkedUser();
        if(user == null)return;
        updateChunkTracking(user, chunkPos, packet, bl1, bl2);
        ci.cancel();
    }

    @Inject(method = "updateChunkTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;untrackChunk(Lnet/minecraft/world/level/ChunkPos;)V"), cancellable = true)
    private void shouldUntrack(ServerPlayer player, ChunkPos chunkPos, MutableObject<ClientboundLevelChunkWithLightPacket> packet, boolean bl1, boolean bl2, CallbackInfo ci){
        ChunkPos playerChunkPos = new ChunkPos(player.getOnPos());
        ChunkPos cameraPos = ServerCameraManager.getCameraChunk(player);
        boolean inPlayerView = Util.isInViewDistance(chunkPos.x, chunkPos.z, viewDistance, playerChunkPos.x, playerChunkPos.z);
        if(cameraPos == null)return;
        boolean inCameraView = Util.isInViewDistance(chunkPos.x, chunkPos.z, viewDistance, cameraPos.x, cameraPos.z);
        if(inPlayerView || inCameraView)ci.cancel();

    }


    @Inject(method = "move", at = @At("TAIL"))
    private void securitycraft$trackCameraLoadedChunks(ServerPlayer player, CallbackInfo ci) {


    }

    /**
     * Allows chunks that are forceloaded near a currently active camera to be sent to the player mounting the camera or viewing
     * the camera feed in a frame.
     */
    @Inject(method = "getPlayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getLastSectionPos()Lnet/minecraft/core/SectionPos;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void securitycraft$sendChunksToCameras(ChunkPos pos, boolean boundaryOnly, CallbackInfoReturnable<List<ServerPlayer>> cir, Set<ServerPlayer> allPlayers, ImmutableList.Builder<ServerPlayer> playerList, Iterator<Player> playerIterator, ServerPlayer player) {
        SectionPos playerPos = player.getLastSectionPos();

        if (!ChunkMap.isChunkInRange(pos.x, pos.z, playerPos.x(), playerPos.z(), viewDistance)) {
            if (ServerCameraManager.isRegistered(player.getUUID())) {
                LevelPos cameraLevelPos = ServerCameraManager.getCamera(player);
                SectionPos cameraPos = SectionPos.of(cameraLevelPos.pos());

                if (Util.isInViewDistance(cameraPos.x(), cameraPos.z(), 10, pos.x, pos.z))
                    playerList.add(player);
            }
        }
    }

    /**
     * Makes sure that chunks in the view area of a frame do not get dropped when the player moves out of them
     */
    @Inject(method = "updateChunkTracking", at = @At("HEAD"), cancellable = true)
    private void securitycraft$onDropChunk(ServerPlayer player, ChunkPos pos, MutableObject<ClientboundLevelChunkWithLightPacket> packetCache, boolean wasLoaded, boolean load, CallbackInfo ci) {
        if (wasLoaded && !load && ServerCameraManager.isRegistered(player.getUUID())) {
            ci.cancel();
        }
    }

}
