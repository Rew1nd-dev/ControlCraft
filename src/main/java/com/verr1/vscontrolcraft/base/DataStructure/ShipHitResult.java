package com.verr1.vscontrolcraft.base.DataStructure;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.valkyrienskies.core.api.ships.Ship;

public record ShipHitResult(
        Vec3 hitLocation,
        Ship ship
) {

}
