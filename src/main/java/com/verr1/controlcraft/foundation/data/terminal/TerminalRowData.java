package com.verr1.controlcraft.foundation.data.terminal;

import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.foundation.type.ExposedFieldType;
import org.joml.Vector2d;

public record TerminalRowData(
        boolean enabled,
        ExposedFieldType type,
        double value,
        Couple<Double> min_max,
        boolean isBoolean,
        boolean isReverse
) {
}
