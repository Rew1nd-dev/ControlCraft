package com.verr1.controlcraft.foundation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BlockEntityGetter {

    public static BlockEntityGetter INSTANCE = null;

    private final MinecraftServer server;

    private BlockEntityGetter(MinecraftServer server){
        this.server = server;
    }

    public static void create(MinecraftServer server){
        INSTANCE = new BlockEntityGetter(server);
    }

    public <T> Optional<T> getBlockEntityAt(GlobalPos globalPos, Class<T> clazz){
        return Optional
                .ofNullable(server.getLevel(globalPos.dimension()))
                .map(world -> world.getExistingBlockEntity(globalPos.pos()))
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }


    public static <T> Optional<T> getLevelBlockEntityAt(@Nullable Level world, @NotNull BlockPos pos, Class<T> clazz){
        return Optional.ofNullable(world)
                .map(w -> w.getExistingBlockEntity(pos))
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }

}
