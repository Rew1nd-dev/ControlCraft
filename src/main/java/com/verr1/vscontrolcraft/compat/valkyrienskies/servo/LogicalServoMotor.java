package com.verr1.vscontrolcraft.compat.valkyrienskies.servo;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Consumer;

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
