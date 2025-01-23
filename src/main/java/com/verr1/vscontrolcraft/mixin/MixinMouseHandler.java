package com.verr1.vscontrolcraft.mixin;


import com.verr1.vscontrolcraft.events.KeyPressEventBus;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    void controlCraft$onMouseButtonPressed(long windowPointer, int button, int action, int modifiers, CallbackInfo ci){
        /*
        KeyPressEventBus.EventCallBack callBack = KeyPressEventBus.fireEvent(new KeyPressEventBus.MouseButtonEvent(windowPointer, button, action, modifiers));
        if(callBack.shouldCancel()){
            ci.cancel();
        }
        */

    }
}
