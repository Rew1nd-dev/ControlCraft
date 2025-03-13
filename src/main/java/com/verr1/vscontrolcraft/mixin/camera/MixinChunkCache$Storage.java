package com.verr1.vscontrolcraft.mixin.camera;

import com.verr1.vscontrolcraft.blocks.camera.LinkedCameraManager;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkCache.Storage.class)
public class MixinChunkCache$Storage {

    @Shadow @Final private int viewRange;

    @Inject(method = "inRange", at = @At("HEAD"), cancellable = true)
    private void controlCraft$inRange(int x, int z, CallbackInfoReturnable<Boolean> cir){
        if(!LinkedCameraManager.isIsLinked())return;
        if(new ChunkPos(x,z).getChessboardDistance(new ChunkPos(LinkedCameraManager.getLinkCameraPos())) > (this.viewRange + 1))return;
        cir.setReturnValue(true);

    }
}
