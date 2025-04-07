package com.verr1.controlcraft.foundation.data.logical;

import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

public record LogicalPropeller(
        boolean canDrive,
        Vector3d direction,
        double speed,
        double THRUST_RATIO,
        double TORQUE_RATIO,
        WorldBlockPos pos
) {

}
