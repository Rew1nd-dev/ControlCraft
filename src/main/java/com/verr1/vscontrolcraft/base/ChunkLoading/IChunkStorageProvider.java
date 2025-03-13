package com.verr1.vscontrolcraft.base.ChunkLoading;

import net.minecraft.client.multiplayer.ClientChunkCache;

public interface IChunkStorageProvider {
    default ClientChunkCache.Storage newStorage(int viewDistance) {
        if (this instanceof ClientChunkCache cache)
            return cache.new Storage(viewDistance);

        return null;
    }
}
