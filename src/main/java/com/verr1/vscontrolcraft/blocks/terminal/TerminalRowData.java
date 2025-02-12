package com.verr1.vscontrolcraft.blocks.terminal;

import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldType;
import org.joml.Vector2d;

public record TerminalRowData(
        boolean enabled,
        ExposedFieldType type,
        double value,
        Vector2d min_max,
        boolean isBoolean
) {
}
