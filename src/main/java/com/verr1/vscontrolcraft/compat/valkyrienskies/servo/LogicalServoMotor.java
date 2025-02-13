package com.verr1.vscontrolcraft.compat.valkyrienskies.servo;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public record LogicalServoMotor(
        long servShipID,
        long compShipID,
        ServerLevel level,
        Direction servDir,
        Direction compDir,
        boolean angleOrSpeed,
        boolean shouldCounter,
        double torque
) {

}
