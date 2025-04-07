package com.verr1.controlcraft.mixinducks;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;

public interface ICameraDuck {
    void controlCraft$setDetached(boolean detached);



    @Unique
    void controlCraft$setupWithShipMounted(@NotNull BlockGetter level, @NotNull Entity renderViewEntity,
                                           boolean thirdPerson, boolean thirdPersonReverse, float partialTicks,
                                           @NotNull ClientShip shipMountedTo, @NotNull Vector3dc inShipPlayerPosition);

    void controlCraft$setRotationWithShipTransform(final float yaw, final float pitch, final ShipTransform renderTransform);

}
