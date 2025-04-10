package com.verr1.controlcraft.content.blocks.loader;

import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.utils.VSGetterUtils;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Optional;

public record WorldChunkPos(String dimensionID, long chunkPosLong) {

    @Override
    public boolean equals(Object o) {
        if(o instanceof WorldChunkPos other) {
            return dimensionID.equals(other.dimensionID) && chunkPosLong == other.chunkPosLong;
        }
        return false;
    }

    public @Nullable ServerLevel serverLevel(){
        return
        Optional.ofNullable(ControlCraftServer.INSTANCE)
                .map(
                    s -> s.getLevel(VSGameUtilsKt.getResourceKey(dimensionID))
                )
                .orElse(null);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(chunkPosLong) ^ dimensionID.hashCode();
    }
}
