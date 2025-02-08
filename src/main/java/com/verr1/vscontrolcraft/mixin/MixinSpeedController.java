package com.verr1.vscontrolcraft.mixin;


import com.simibubi.create.compat.computercraft.implementation.peripherals.SpeedControllerPeripheral;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.verr1.vscontrolcraft.Config;
import com.verr1.vscontrolcraft.compat.cctweaked.alternates.ComputerCraftDelegation;
import dan200.computercraft.api.lua.LuaFunction;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
 *   Originally, when calling methods with @LuaFunction(mainThread = true), ComputerCraft
 *   will put it into a queue which will be polled in its own mainThread, since CC
 *   mainThread will be synced with VS physics thread now if Config.OverclockComputerCraft == true, so
 *   those methods will be executed in delegate thread rather than game main thread.
 *
 *   So If any peripheral breaks in execution of delegate thread, mixin broken peripherals
 *   like this to queue their method body in ** Some Kind Of Main Thread Scheduler **
 */

@Mixin(SpeedControllerPeripheral.class)
public abstract class MixinSpeedController {

    @Final
    @Shadow(remap = false)
    private ScrollValueBehaviour targetSpeed;

    @Inject(method = "setTargetSpeed", at = @At("HEAD"), remap = false, cancellable = true)
    void controlCraft$setTargetSpeedAsync(int speed, CallbackInfo ci){
        if(Config.OverclockComputerCraft){
            ComputerCraftDelegation.issueMainThreadTask(() -> targetSpeed.setValue(speed));
            ci.cancel();
        }
    }


}
