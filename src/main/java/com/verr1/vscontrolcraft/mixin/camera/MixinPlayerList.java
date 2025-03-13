package com.verr1.vscontrolcraft.mixin.camera;


import com.verr1.vscontrolcraft.blocks.camera.ServerCameraManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = PlayerList.class, priority = 1100)
public class MixinPlayerList {

    @Inject(method = "broadcast", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/level/ServerPlayer;getZ()D"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void securitycraft$broadcastToCameras(Player except, double x, double y, double z, double radius, ResourceKey<Level> dimension, Packet<?> packet, CallbackInfo ci, int iteration, ServerPlayer player) {
        if (ServerCameraManager.isRegistered(player.getUUID())) {
            BlockPos camera = ServerCameraManager.getCamera(player).pos();
            double dX = x - camera.getX();
            double dY = y - camera.getY();
            double dZ = z - camera.getZ();

            if (dX * dX + dY * dY + dZ * dZ < radius * radius)
                player.connection.send(packet);

            ci.cancel();
        }
    }

}
