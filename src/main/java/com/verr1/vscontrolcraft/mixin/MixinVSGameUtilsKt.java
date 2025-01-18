package com.verr1.vscontrolcraft.mixin;

import com.verr1.vscontrolcraft.blocks.camera.LinkedCameraManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.entity.ShipMountedToData;

@Mixin(VSGameUtilsKt.class)
public abstract class MixinVSGameUtilsKt {

    // Disable VS Camera Setup When Player is using Camera Block while on ship
    @Inject(method = "getShipMountedToData", at = @At("RETURN"), remap = false, cancellable = true)
    private static void controlCraft$getShipMountedToData(Entity passenger, Float partialTicks, CallbackInfoReturnable<ShipMountedToData> cir){
        if(passenger instanceof LocalPlayer && passenger.level().isClientSide && LinkedCameraManager.getLinkedCamera() != null){
            cir.setReturnValue(null);
        }
    }

}
