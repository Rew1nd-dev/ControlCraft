package com.verr1.controlcraft.mixin;

import com.verr1.controlcraft.events.ControlCraftEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(org.valkyrienskies.core.impl.shadow.At.class)
abstract public class MixinAt {

    @Inject(method="a(Lorg/joml/Vector3dc;D)V", at = @At("HEAD"), remap = false)
    void ControlCraft$onPhysicsTickStart(CallbackInfo ci) {
        ControlCraftEvents.onPhysicsTickStart();
    }

    @Inject(method="a(Lorg/joml/Vector3dc;D)V", at = @At("TAIL"), remap = false)
    void ControlCraft$onPhysicsTickEnd(CallbackInfo ci) {
        ControlCraftEvents.onPhysicsTickEnd();
    }

}
