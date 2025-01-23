package com.verr1.vscontrolcraft.base.DataStructure;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record LevelPos(BlockPos pos, ServerLevel level) {
    @Override
    public int hashCode() {
        return pos.hashCode();
    }
}
