package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;

public interface ISpatialTarget {

    ServerLevel level();

    BlockPos pos();

    default LevelPos levelPos(){
        return new LevelPos(pos(), level());
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
