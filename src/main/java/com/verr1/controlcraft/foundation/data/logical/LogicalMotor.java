package com.verr1.controlcraft.foundation.data.logical;

import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.control.Controller;
import net.minecraft.core.Direction;

public record LogicalMotor(
        long motorShipID,
        long compShipID,
        WorldBlockPos pos,
        Direction motorDir,
        Direction compDir,
        boolean angleOrSpeed,
        boolean shouldCounter,
        boolean free,
        double torque,
        Controller controller
){

}
