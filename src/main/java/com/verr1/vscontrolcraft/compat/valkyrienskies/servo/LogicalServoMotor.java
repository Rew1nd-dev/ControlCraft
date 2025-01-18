package com.verr1.vscontrolcraft.compat.valkyrienskies.servo;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public record LogicalServoMotor(long servoShipID, long assembledShipID, ServerLevel level, Direction servoDir, Direction assemDir) {
}
