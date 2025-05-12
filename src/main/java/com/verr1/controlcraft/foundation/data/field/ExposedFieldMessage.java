package com.verr1.controlcraft.foundation.data.field;

import com.verr1.controlcraft.foundation.type.descriptive.SlotDirection;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;

public record ExposedFieldMessage(
        SlotType type,
        double min,
        double max,
        SlotDirection openTo) {
}
