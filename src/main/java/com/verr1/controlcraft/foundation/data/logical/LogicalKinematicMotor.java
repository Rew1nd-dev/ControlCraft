package com.verr1.controlcraft.foundation.data.logical;

import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.constraint.ConnectContext;
import com.verr1.controlcraft.foundation.data.control.KinematicController;
import net.minecraft.core.Direction;

public record LogicalKinematicMotor(
        long motorShipID,
        long compShipID,
        ConnectContext context,
        boolean angleOrSpeed,
        Direction servoDir,
        Direction compAlign,
        KinematicController controller
) {

}
