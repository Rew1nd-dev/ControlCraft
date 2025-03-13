package com.verr1.controlcraft.foundation.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.valkyrienskies.mod.api.ValkyrienSkies;

import javax.annotation.Nullable;
import java.util.Optional;

public record WorldBlockPos(GlobalPos globalPos) {
    public static WorldBlockPos of(Level level, BlockPos pos){
        return new WorldBlockPos(GlobalPos.of(level.dimension(), pos));
    }

    public @Nullable ServerLevel level(MinecraftServer server){
        return server.getLevel(globalPos.dimension());
    }


    public BlockPos pos(){
        return globalPos.pos();
    }
}
