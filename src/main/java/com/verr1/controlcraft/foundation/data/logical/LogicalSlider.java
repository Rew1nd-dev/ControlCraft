package com.verr1.controlcraft.foundation.data.logical;

import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.control.Controller;
import net.minecraft.core.Direction;
import org.joml.Vector3dc;

public record LogicalSlider (
        long selfShipID,
        long compShipID,
        WorldBlockPos pos,
        Direction slideDir,
        Vector3dc selfContact,
        Vector3dc compContact,
        boolean positionOrSpeed,
        boolean shouldCounter,
        boolean free,
        double force,
        Controller controller
){
}
