package com.verr1.controlcraft.mixin.tweak;


import com.getitemfromblock.create_tweaked_controllers.block.TweakedLecternControllerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TweakedLecternControllerBlockEntity.class)
public class MixinTweakedLecternControllerBlockEntity {


    @Inject(method = "shouldUseFullPrecision", at = @At("HEAD"), remap = false, cancellable = true)
    void controlCraft$shouldUseFullPrecision(CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(true);
        cir.cancel();
    }

}
