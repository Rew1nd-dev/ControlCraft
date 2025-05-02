package com.verr1.controlcraft.foundation.data;

import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.Ship;

public record ShipHitResult(
        Vec3 hitLocation,
        Ship ship
) {

}