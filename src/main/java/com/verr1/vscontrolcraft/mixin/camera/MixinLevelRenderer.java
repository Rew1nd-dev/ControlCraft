package com.verr1.vscontrolcraft.mixin.camera;


import com.verr1.vscontrolcraft.base.ChunkLoading.CameraViewAreaExtension;
import com.verr1.vscontrolcraft.blocks.camera.LinkedCameraManager;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Shadow
    private ChunkRenderDispatcher chunkRenderDispatcher;
    @Shadow
    private ClientLevel level;

    /**
     * When rendering the world in a frame, the necessary visible sections are captured manually within SecurityCraft. Vanilla
     * usually does the same process in setupRender, so that method is exited early when a frame feed is rendered. However, when
     * Embeddium or Sodium is installed, these mods may perform their visible section capture themselves since it's much more
     * performant, and since that happens in setupRender too, the method is not exited early in this case.
     @Inject(method = "setupRender", at = @At("HEAD"), cancellable = true)
     private void securitycraft$onSetupRender(Camera camera, Frustum frustum, boolean hasCapturedFrustum, boolean isSpectator, CallbackInfo ci) {
     if (LinkedCameraManager.isIsLinked())
     ci.cancel();
     }
     */


    /**
     * Updates the camera view area with the refreshed section render dispatcher when F3+A is pressed
     */
    @Inject(method = "allChanged", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/atomic/AtomicReference;set(Ljava/lang/Object;)V"))
    private void securitycraft$onAllChanged(CallbackInfo ci) {
        CameraViewAreaExtension.allChanged(chunkRenderDispatcher, level);
    }

    /**
     * If rendering a frame camera, makes sure that all compiled sections within the camera view area extension are properly
     * treated as compiled (e.g. for the purpose of entity rendering)
     */
    @Inject(method = "isChunkCompiled", at = @At("HEAD"), cancellable = true)
    private void securitycraft$onIsSectionCompiled(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        if (LinkedCameraManager.isIsLinked()) {
            SectionPos sectionPos = SectionPos.of(pos);
            ChunkRenderDispatcher.RenderChunk renderSection = CameraViewAreaExtension.rawFetch(sectionPos.x(), sectionPos.y(), sectionPos.z(), false);

            if (renderSection != null && renderSection.compiled.get() != ChunkRenderDispatcher.CompiledChunk.UNCOMPILED)
                ci.setReturnValue(true);
        }
    }

    /**
     * Sets the correct fog distance for rendering a frame feed, depending on clientside view distance configuration settings.
     * Note that the frame block entity chunk loading distance option is not respected for this, since it is only supposed to
     * affect the server by setting a limit on forceloaded chunks and unfit to be handled on the client side.
     */


}
