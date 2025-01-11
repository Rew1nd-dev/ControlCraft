package com.verr1.vscontrolcraft.compat.valkyrienskies.spnialyzer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record LogicalSensor(ServerLevel level, BlockPos pos) {
    public int hashCode(){
        return pos.hashCode();
    }
}
