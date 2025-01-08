package com.verr1.vscontrolcraft.blocks.chunkLoader;

import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

public record ChunkLevelPos(ServerLevel serverLevel, long chunkPosLong) {
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ChunkLevelPos)) return false;
        ChunkLevelPos other = (ChunkLevelPos) o;

        Object this$serverLevel = this.serverLevel;
        Object other$serverLevel = other.serverLevel;
        if (!Objects.equals(this$serverLevel, other$serverLevel))
            return false;
        if (this.chunkPosLong != other.chunkPosLong) return false;
        return true;
    }

    public int hashCode() {
        return Long.hashCode(chunkPosLong);
    }
}
