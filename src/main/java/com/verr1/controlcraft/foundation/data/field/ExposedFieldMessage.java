package com.verr1.controlcraft.foundation.data.field;

import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldDirection;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;

public record ExposedFieldMessage(
        ExposedFieldType type,
        double min,
        double max,
        ExposedFieldDirection openTo) {
}
