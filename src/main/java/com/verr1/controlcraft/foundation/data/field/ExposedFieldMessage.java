package com.verr1.controlcraft.foundation.data.field;

import com.verr1.controlcraft.foundation.type.ExposedFieldDirection;
import com.verr1.controlcraft.foundation.type.ExposedFieldType;

public record ExposedFieldMessage(
        ExposedFieldType type,
        double min,
        double max,
        ExposedFieldDirection openTo) {
}
