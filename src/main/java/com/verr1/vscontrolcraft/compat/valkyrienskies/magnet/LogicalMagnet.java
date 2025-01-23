package com.verr1.vscontrolcraft.compat.valkyrienskies.magnet;


import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record LogicalMagnet(BlockPos pos, ServerLevel level) {
    @Override
    public int hashCode() {
        return pos.hashCode();
    }
}
