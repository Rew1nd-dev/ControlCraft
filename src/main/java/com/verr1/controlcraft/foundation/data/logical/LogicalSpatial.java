package com.verr1.controlcraft.foundation.data.logical;

import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.foundation.api.ISpatialTarget;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.control.SpatialSchedule;
import com.verr1.controlcraft.utils.VSGetterUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;

import javax.annotation.Nullable;
import java.util.Optional;

public record LogicalSpatial(
        WorldBlockPos worldBlockPos,
        Direction align,
        Direction forward,
        long shipID,
        String dimensionID,
        boolean shouldDrive,
        boolean isStatic,
        long protocol,
        SpatialSchedule schedule
) implements ISpatialTarget {
    @Override
    public @Nullable ServerLevel level() {
        return Optional.ofNullable(ControlCraftServer.INSTANCE).map(worldBlockPos::level).orElse(null);
    }

    @Override
    public BlockPos pos() {
        return worldBlockPos.pos();
    }

    public WorldBlockPos levelPos(){
        return worldBlockPos;
    }

    @Override
    public Quaterniondc qBase(){
        // ControlCraft.LOGGER.info("qBase: " + VSMathUtils.getQuaternion(levelPos()));
        return VSGetterUtils.getQuaternion(levelPos());
    }

    @Override
    public Vector3dc vPos(){
        return VSGetterUtils.getAbsolutePosition(levelPos());
    }


    @Override
    public int hashCode() {
        return worldBlockPos.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof LogicalSpatial so))return false;
        return worldBlockPos.equals(so.worldBlockPos);
    }
}
