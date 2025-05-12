package com.verr1.controlcraft.foundation.data.terminal;

import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;

public record TerminalRowData(
        boolean enabled,
        SlotType type,
        double value,
        Couple<Double> min_max,
        boolean isBoolean,
        boolean isReversed
) {
}
