package com.verr1.controlcraft.foundation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ServerBlockEntityGetter {

    public static ServerBlockEntityGetter INSTANCE = null;

    private final MinecraftServer server;

    private ServerBlockEntityGetter(MinecraftServer server){
        this.server = server;
    }

    public static void create(MinecraftServer server){
        INSTANCE = new ServerBlockEntityGetter(server);
    }

    public <T> Optional<T> getBlockEntityAt(GlobalPos globalPos, Class<T> clazz){
        return Optional
                .ofNullable(server.getLevel(globalPos.dimension()))
                .map(world -> world.getExistingBlockEntity(globalPos.pos()))
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }


    public <T> Optional<T> getBlockEntityAt(@NotNull ServerLevel world, @NotNull BlockPos pos, Class<T> clazz){
        return Optional
                .ofNullable(world.getExistingBlockEntity(pos))
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }

}
