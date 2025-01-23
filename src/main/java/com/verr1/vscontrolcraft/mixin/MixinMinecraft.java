package com.verr1.vscontrolcraft.mixin;


import com.verr1.vscontrolcraft.base.Wand.ClientWand;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    /*
    @Inject(method = "startAttack", at = @At("HEAD"))
    void controlCraft$startAttack(CallbackInfoReturnable<Boolean> cir){
        if(ClientWand.isClientWandInHand()){
            cir.cancel();
        }
    }

    @Inject(method="startUseItem", at=@At("HEAD"), cancellable = true)
    void controlCraft$startUseItem(CallbackInfo ci){
        if(ClientWand.isClientWandInHand()){
            ci.cancel();
        }
    }
    * */


}
