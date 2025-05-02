package com.verr1.controlcraft.foundation.data.logical;

import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.control.DynamicController;
import net.minecraft.core.Direction;

public record LogicalDynamicMotor(
        long motorShipID,
        long compShipID,
        WorldBlockPos pos,
        Direction motorDir,
        Direction compDir,
        boolean angleOrSpeed,
        boolean shouldCounter,
        boolean eliminateGravity,
        boolean free,
        double torque,
        DynamicController controller
){

}
