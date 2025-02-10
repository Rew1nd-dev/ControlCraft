package com.verr1.vscontrolcraft.blocks.terminal;

import org.joml.Vector2d;

public record TerminalRowData(boolean enabled, String name, double value, Vector2d min_max, boolean isBoolean) {
}
