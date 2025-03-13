package com.verr1.controlcraft.foundation.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record WandSelection(BlockPos pos, Direction face) {
    public static final WandSelection NULL = new WandSelection(null, null);
}
