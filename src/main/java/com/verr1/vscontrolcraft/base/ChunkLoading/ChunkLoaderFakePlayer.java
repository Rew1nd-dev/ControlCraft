package com.verr1.vscontrolcraft.base.ChunkLoading;

import com.mojang.authlib.GameProfile;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.blocks.camera.CameraBlockEntity;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.util.FakePlayer;
import org.checkerframework.checker.units.qual.degrees;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ChunkLoaderFakePlayer extends FakePlayer {

    public ServerPlayer getLinkedUser() {
        return linkedUser;
    }

    private ServerPlayer linkedUser;
    private BlockPos boundCameraPos;

    public ChunkLoaderFakePlayer(ServerLevel level, GameProfile name, BlockPos blockPos) {
        super(level, name);
        boundCameraPos = blockPos;
    }


    @Override
    public void setLastSectionPos(SectionPos p_9120_) {
        super.setLastSectionPos(p_9120_);
    }

    public void redirectConnection(ServerPlayer owner){
        linkedUser = owner;
        this.moveTo(owner.getPosition(1));
        DeferralExecutor.executeLater(() -> {
            if(getBoundCamera() != null){
                this.moveTo(getBoundCamera().getBlockPos().getCenter());
                }
            }, 2
        );
        //setLastSectionPos(owner.getLastSectionPos());
    }

    public CameraBlockEntity getBoundCamera(){
        if(serverLevel().getExistingBlockEntity(boundCameraPos) instanceof CameraBlockEntity camera)return camera;
        return null;
    }

    public void redirectConnection(){
        linkedUser = null;
    }

    @Override
    public void tick() {
        Entity entity = this.getCamera();
        if (entity != this) {
            if (entity.isAlive()) {
                this.absMoveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                this.serverLevel().getChunkSource().move(this);
            } else {
                this.setCamera(this);
            }
        }
    }

    @ParametersAreNonnullByDefault
    private static class ChunkLoaderPacketListenerImpl extends ServerGamePacketListenerImpl{
        private static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);

        public ChunkLoaderPacketListenerImpl(MinecraftServer server, ServerPlayer player) {
            super(server, player.connection.connection, player);
        }

        public ChunkLoaderPacketListenerImpl(MinecraftServer server, ServerPlayer player, boolean dummy) {
            super(server, DUMMY_CONNECTION, player);
        }

        @Override public void tick() { }
        @Override public void resetPosition() { }
        @Override public void disconnect(Component message) { }
        @Override public void handlePlayerInput(ServerboundPlayerInputPacket packet) { }
        @Override public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) { }
        @Override public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) { }
        @Override public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) { }
        @Override public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) { }
        @Override public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) { }
        @Override public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) { }
        @Override public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) { }
        @Override public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) { }
        @Override public void handlePickItem(ServerboundPickItemPacket packet) { }
        @Override public void handleRenameItem(ServerboundRenameItemPacket packet) { }
        @Override public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) { }
        @Override public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) { }
        @Override public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) { }
        @Override public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) { }
        @Override public void handleSelectTrade(ServerboundSelectTradePacket packet) { }
        @Override public void handleEditBook(ServerboundEditBookPacket packet) { }
        @Override public void handleEntityTagQuery(ServerboundEntityTagQuery packet) { }
        @Override public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery packet) { }
        @Override public void handleMovePlayer(ServerboundMovePlayerPacket packet) { }
        @Override public void teleport(double x, double y, double z, float yaw, float pitch) { }
        @Override public void handlePlayerAction(ServerboundPlayerActionPacket packet) { }
        @Override public void handleUseItemOn(ServerboundUseItemOnPacket packet) { }
        @Override public void handleUseItem(ServerboundUseItemPacket packet) { }
        @Override public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) { }
        @Override public void handleResourcePackResponse(ServerboundResourcePackPacket packet) { }
        @Override public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) { }
        @Override public void onDisconnect(Component message) { }

        @Override public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) { }
        @Override public void handleChat(ServerboundChatPacket packet) { }
        @Override public void handleAnimate(ServerboundSwingPacket packet) { }
        @Override public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) { }
        @Override public void handleInteract(ServerboundInteractPacket packet) { }
        @Override public void handleClientCommand(ServerboundClientCommandPacket packet) { }
        @Override public void handleContainerClose(ServerboundContainerClosePacket packet) { }
        @Override public void handleContainerClick(ServerboundContainerClickPacket packet) { }
        @Override public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) { }
        @Override public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) { }
        @Override public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) { }
        @Override public void handleSignUpdate(ServerboundSignUpdatePacket packet) { }
        @Override public void handleKeepAlive(ServerboundKeepAlivePacket packet) { }
        @Override public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) { }
        @Override public void handleClientInformation(ServerboundClientInformationPacket packet) { }
        @Override public void handleCustomPayload(ServerboundCustomPayloadPacket packet) { }
        @Override public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) { }
        @Override public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) { }
        @Override public void teleport(double x, double y, double z, float yaw, float pitch, Set<RelativeMovement> relativeSet) { }
        @Override public void ackBlockChangesUpTo(int sequence) { }
        @Override public void handleChatCommand(ServerboundChatCommandPacket packet) { }
        @Override public void handleChatAck(ServerboundChatAckPacket packet) { }
        @Override public void addPendingMessage(PlayerChatMessage message) { }
        @Override public void sendPlayerChatMessage(PlayerChatMessage message, ChatType.Bound boundChatType) { }
        @Override public void sendDisguisedChatMessage(Component content, ChatType.Bound boundChatType) { }
        @Override public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket packet) { }



        @Override public void send(Packet<?> packet, @Nullable PacketSendListener sendListener) {
            if(packet instanceof ClientboundAddPlayerPacket) return;

            if(!(
                    packet instanceof ClientboundLevelChunkWithLightPacket ||
                    packet instanceof ClientboundLevelChunkPacketData
            ))return;


            super.send(packet, sendListener);
        }

    }

}
