package com.verr1.controlcraft.mixin;

import com.verr1.controlcraft.mixinducks.IEntityDuck;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity implements IEntityDuck {


    @Shadow
    public abstract int getId();

    @Shadow public abstract Vec3 position();

    @Unique
    private Vec3 controlCraftOldVS$velocity = new Vec3(0, 0, 0);

    @Unique
    private int controlCraft$clientGlowingTick = 0;

    @Unique private Vec3 controlCraft$positionOld = Vec3.ZERO;

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    void shouldAppearGlowing(CallbackInfoReturnable<Boolean> cir){
        if(controlCraft$clientGlowingTick > 0){
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    void tick(CallbackInfo ci) {
        controlCraftOldVS$tickObserver();
    }

    @Override
    public void controlCraft$setClientGlowing(int duration){
        controlCraft$clientGlowingTick = duration;
    }

    @Override
    public Vec3 controlCraft$velocityObserver(){
        return controlCraftOldVS$velocity;
    }

    @Override
    public void controlCraftOldVS$tickObserver() {
        if(controlCraft$clientGlowingTick > 0)controlCraft$clientGlowingTick--;
        controlCraftOldVS$velocity = position().subtract(controlCraft$positionOld).scale(20f);
        controlCraft$positionOld = new Vec3(position().x(), position().y(), position().z());

    }
}
