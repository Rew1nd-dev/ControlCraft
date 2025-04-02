package com.verr1.controlcraft.foundation.data.logical;

import com.verr1.controlcraft.foundation.data.WorldBlockPos;

public record LogicalAnchor(double airResist, double extraGravity, double rotDamp, WorldBlockPos pos, boolean airResistAtPos, boolean extraGravityAtPos) {
}
