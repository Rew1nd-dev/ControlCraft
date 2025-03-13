package com.verr1.vscontrolcraft.mixin.camera;

import com.verr1.vscontrolcraft.blocks.camera.ServerCameraManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ServerPlayer.class, priority = 1100)
abstract class MixinServerPlayer {



    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;absMoveTo(DDDFF)V"))
    private void securitycraft$tick(ServerPlayer player, double x, double y, double z, float yaw, float pitch) {
        if (!ServerCameraManager.isRegistered(player.getUUID()))
            player.absMoveTo(x, y, z, yaw, pitch);
    }


}
