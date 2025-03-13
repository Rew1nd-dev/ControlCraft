package com.verr1.controlcraft.foundation.api;

import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;

public interface ISpatialTarget {

    ServerLevel level();

    BlockPos pos();

    default WorldBlockPos levelPos(){
        return WorldBlockPos.of(level(), pos());
    }

    Direction align();

    Direction forward();

    long shipID();

    String dimensionID();

    boolean isStatic();

    long protocol();

    Quaterniondc qBase();

    Vector3dc vPos();


}
