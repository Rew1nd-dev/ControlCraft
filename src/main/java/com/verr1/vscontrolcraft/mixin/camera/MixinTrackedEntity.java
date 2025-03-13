package com.verr1.vscontrolcraft.mixin.camera;

import com.verr1.vscontrolcraft.blocks.camera.ServerCameraManager;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ChunkMap.TrackedEntity.class, priority = 1100)
abstract class MixinTrackedEntity {
    @Shadow
    @Final
    Entity entity;
    @Unique
    private boolean controlCraft$shouldBeSent = false;

    /**
     * Checks if this entity is in range of a camera that is currently being viewed, and stores the result in the field
     * shouldBeSent
     */
    @Inject(method = "updatePlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/world/phys/Vec3;x:D", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void securitycraft$onUpdatePlayer(ServerPlayer player, CallbackInfo ci, Vec3 unused, double viewDistance) {
        if (ServerCameraManager.isRegistered(player.getUUID())) {

            Vec3 relativePosToCamera =ServerCameraManager.getCamera(player).pos().getCenter().subtract(entity.position());

            if (relativePosToCamera.x >= -viewDistance && relativePosToCamera.x <= viewDistance && relativePosToCamera.z >= -viewDistance && relativePosToCamera.z <= viewDistance)
                controlCraft$shouldBeSent = true;
        }
    }

    /**
     * Enables entities that should be sent as well as security camera entities to be sent to the client
     */
    @ModifyVariable(method = "updatePlayer", name = "flag", at = @At(value = "JUMP", opcode = Opcodes.IFEQ, shift = At.Shift.BEFORE, ordinal = 1))
    private boolean securitycraft$modifyFlag(boolean original) {
        if (controlCraft$shouldBeSent) {
            controlCraft$shouldBeSent = false;
            return true;
        }

        return original;
    }
}
