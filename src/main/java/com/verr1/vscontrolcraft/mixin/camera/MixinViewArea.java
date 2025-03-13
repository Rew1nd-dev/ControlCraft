package com.verr1.vscontrolcraft.mixin.camera;


import com.verr1.vscontrolcraft.base.ChunkLoading.CameraViewAreaExtension;
import com.verr1.vscontrolcraft.blocks.camera.LinkedCameraManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ViewArea;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ViewArea.class, priority = 1100)
public class MixinViewArea {
    /**
     * Marks chunks within the frame camera view area as dirty when e.g. a block has been changed in them, so the frame feed
     * updates appropriately
     */
    @Inject(method = "setDirty", at = @At("HEAD"))
    private void securitycraft$onSetChunkDirty(int cx, int cy, int cz, boolean reRenderOnMainThread, CallbackInfo ci) {
        CameraViewAreaExtension.setDirty(cx, cy, cz, reRenderOnMainThread);
    }

    /**
     * Fixes camera chunks disappearing when the player entity moves while viewing a camera (e.g. while being in a minecart or
     * falling).
     */
    @Inject(method = "repositionCamera", at = @At("HEAD"), cancellable = true)
    private void securitycraft$preventCameraRepositioning(double x, double z, CallbackInfo ci) {
        if (LinkedCameraManager.isIsLinked())
            ci.cancel();
    }

}
