package com.verr1.vscontrolcraft.mixinDuck;


import net.minecraft.world.phys.Vec3;

public interface EntityAccessor {

    void controlCraft$setClientGlowing(int duration);

    Vec3 controlCraft$velocityObserver();

}
