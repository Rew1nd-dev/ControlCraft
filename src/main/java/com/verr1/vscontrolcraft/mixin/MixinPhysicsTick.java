package com.verr1.vscontrolcraft.mixin;


import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.events.ControlCraftEvents;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;


import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.core.impl.shadow.Ao;

@Pseudo
@Mixin(org.valkyrienskies.core.impl.shadow.Aq.class)
abstract public class MixinPhysicsTick {

    @Inject(method = "a(Lorg/joml/Vector3dc;DZ)Lorg/valkyrienskies/core/impl/shadow/Ao;", at = @At("HEAD"), remap = false)
    void ControlCraft$onPhysicsTick(Vector3dc par1, double par2, boolean par3, CallbackInfoReturnable<Ao> cir) {
        ControlCraftEvents.onPhysicsTick();
    }
}
