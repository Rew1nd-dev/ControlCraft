package com.verr1.controlcraft.foundation.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import javax.annotation.Nullable;

public record WorldBlockPos(String dimensionID, BlockPos pos) {
    public static WorldBlockPos of(Level level, BlockPos pos){
        String d = VSGameUtilsKt.getDimensionId(level);
        return new WorldBlockPos(d, pos);
    }

    public GlobalPos globalPos(){
        return GlobalPos.of(key(), pos);
    }

    private ResourceKey<Level> key(){
        return VSGameUtilsKt.getResourceKey(dimensionID);
    }

    public @Nullable ServerLevel level(@NotNull MinecraftServer server){
        return server.getLevel(key());
    }



}
