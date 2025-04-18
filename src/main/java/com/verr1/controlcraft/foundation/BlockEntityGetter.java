package com.verr1.controlcraft.foundation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.verr1.controlcraft.content.blocks.receiver.PeripheralInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BlockEntityGetter {

    public static BlockEntityGetter INSTANCE = null;

    private final MinecraftServer server;

    private final LoadingCache<GlobalPos, Optional<BlockEntity>> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build(
                    new CacheLoader<>() {
                        @Override
                        public @NotNull Optional<BlockEntity> load(@NotNull GlobalPos pos) {
                            return Optional.ofNullable(server.getLevel(pos.dimension()))
                                    .map(level -> level.getExistingBlockEntity(pos.pos()));
                        }
                    });


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

    public <T> Optional<T> getCachedBlockEntityAt(GlobalPos globalPos, Class<T> clazz){
        try{
            return cache
                    .get(globalPos)
                    .filter(clazz::isInstance)
                    .map(clazz::cast);
        }catch (ExecutionException e){
            return Optional.empty();
        }
    }


    public static <T> Optional<T> getLevelBlockEntityAt(@Nullable Level world, @NotNull BlockPos pos, Class<T> clazz){
        return Optional.ofNullable(world)
                .map(w -> w.getExistingBlockEntity(pos))
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }

}
