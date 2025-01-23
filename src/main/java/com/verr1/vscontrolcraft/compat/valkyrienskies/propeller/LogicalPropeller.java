package com.verr1.vscontrolcraft.compat.valkyrienskies.propeller;

import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3d;

import java.util.function.Supplier;

public record LogicalPropeller(
        boolean canDrive,
        boolean reverseTorque,
        Vector3d direction,
        Supplier<Double> speedCallBack,
        double THRUST_RATIO,
        double TORQUE_RATIO,
        ServerLevel serverLevel
) {

}
