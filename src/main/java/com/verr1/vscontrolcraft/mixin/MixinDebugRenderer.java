package com.verr1.vscontrolcraft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
abstract class MixinDebugRenderer {

    @Final
    @Shadow
    public DebugRenderer.SimpleDebugRenderer chunkRenderer;

    @Inject(method = "render", at = @At("HEAD") )
    void renderTicket(PoseStack p_113458_, MultiBufferSource.BufferSource p_113459_, double p_113460_, double p_113461_, double p_113462_, CallbackInfo ci){
        if(!Minecraft.getInstance().showOnlyReducedInfo()){
            // this.chunkRenderer.render(p_113458_, p_113459_, p_113460_, p_113461_, p_113462_);
        }
    }
}
