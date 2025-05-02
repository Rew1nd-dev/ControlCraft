package com.verr1.controlcraft.foundation.data.logical;

import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import org.joml.Vector3d;

public record LogicalJet(Vector3d direction, double thrust, WorldBlockPos pos) {
}
