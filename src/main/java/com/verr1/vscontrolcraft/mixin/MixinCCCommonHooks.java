package com.verr1.vscontrolcraft.mixin;


import com.verr1.vscontrolcraft.Config;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.compat.cctweaked.alternates.ComputerCraftDelegation;
import dan200.computercraft.shared.CommonHooks;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommonHooks.class)
abstract class MixinCCCommonHooks {

    @Inject(method = "onServerTickStart", at = @At("HEAD"), remap = false, cancellable = true)
    private static void ControlCraft$delegateToPhysicsTick(CallbackInfo ci) {
        if(!Config.OverclockComputerCraft){
            return;
        }
        ci.cancel();
    }

    @Inject(method = "onServerTickEnd", at = @At("HEAD"), remap = false, cancellable = true)
    private static void ControlCraft$delegateToPhysicsTickEnd(CallbackInfo ci) {
        if(!Config.OverclockComputerCraft){
            return;
        }
        ci.cancel();
    }

    @Inject(method = "onServerStarting", at = @At("TAIL"), remap = false)
    private static void ControlCraft$onServerStarting(MinecraftServer server, CallbackInfo ci) {
        if(!Config.OverclockComputerCraft){
            return;
        }
        ComputerCraftDelegation.setServer(server);
        DeferralExecutor.executeLater(ComputerCraftDelegation::DelegateThreadStart, 10);
    }

    @Inject(method = "onServerStopped", at = @At("HEAD"), remap = false)
    private static void ControlCraft$onServerStopped(CallbackInfo ci) {
        if(!Config.OverclockComputerCraft){
            return;
        }
        ComputerCraftDelegation.DelegateThreadKill();
    }

}
