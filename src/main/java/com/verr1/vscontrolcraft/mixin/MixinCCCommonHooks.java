package com.verr1.vscontrolcraft.mixin;


import dan200.computercraft.shared.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommonHooks.class)
abstract class MixinCCCommonHooks {

    @Inject(method = "onServerTickStart", at = @At("HEAD"), remap = false, cancellable = true)
    private static void ControlCraft$delegateToPhysicsTick(CallbackInfo ci) {
        ci.cancel();
    }

}
