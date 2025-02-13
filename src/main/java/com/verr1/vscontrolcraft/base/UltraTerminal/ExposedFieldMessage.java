package com.verr1.vscontrolcraft.base.UltraTerminal;

public record ExposedFieldMessage(
        ExposedFieldType type,
        double min,
        double max,
        ExposedFieldDirection openTo) {
}
