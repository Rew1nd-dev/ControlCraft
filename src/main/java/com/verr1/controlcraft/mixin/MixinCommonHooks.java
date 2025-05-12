package com.verr1.controlcraft.mixin;

import com.verr1.controlcraft.config.BlockPropertyConfig;
import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.content.cctweaked.delegation.ComputerCraftDelegation;
import dan200.computercraft.shared.CommonHooks;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommonHooks.class)
abstract class MixinCommonHooks {

    @Inject(method = "onServerTickStart", at = @At("HEAD"), remap = false, cancellable = true)
    private static void ControlCraft$delegateToPhysicsTick(CallbackInfo ci) {
        if(!BlockPropertyConfig._CC_OVERCLOCKING){
            return;
        }
        ci.cancel();
    }

    @Inject(method = "onServerTickEnd", at = @At("HEAD"), remap = false, cancellable = true)
    private static void ControlCraft$delegateToPhysicsTickEnd(CallbackInfo ci) {
        if(!BlockPropertyConfig._CC_OVERCLOCKING){
            return;
        }
        ci.cancel();
    }

    @Inject(method = "onServerStarting", at = @At("TAIL"), remap = false)
    private static void ControlCraft$onServerStarting(MinecraftServer server, CallbackInfo ci) {
        if(!BlockPropertyConfig._CC_OVERCLOCKING){
            return;
        }
        ComputerCraftDelegation.setServer(server);
        ControlCraftServer.SERVER_EXECUTOR.executeLater(ComputerCraftDelegation::delegateThreadStart, 10);
    }

    @Inject(method = "onServerStopped", at = @At("HEAD"), remap = false)
    private static void ControlCraft$onServerStopped(CallbackInfo ci) {
        if(!BlockPropertyConfig._CC_OVERCLOCKING){
            return;
        }
        ComputerCraftDelegation.delegateThreadKill();
    }

}
