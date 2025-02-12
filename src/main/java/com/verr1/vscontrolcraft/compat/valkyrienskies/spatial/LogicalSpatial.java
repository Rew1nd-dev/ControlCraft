package com.verr1.vscontrolcraft.compat.valkyrienskies.spatial;

import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.ISpatialTarget;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public record LogicalSpatial(
        ServerLevel level,
        BlockPos pos,
        Direction align,
        Direction forward,
        long shipID,
        String dimensionID,
        boolean shouldDrive,
        boolean isStatic,
        long protocol
) implements ISpatialTarget {
    public LevelPos levelPos(){
        return new LevelPos(pos, level);
    }

    @Override
    public Quaterniondc qBase(){
        // ControlCraft.LOGGER.info("qBase: " + VSMathUtils.getQuaternion(levelPos()));
        return VSMathUtils.getQuaternion(levelPos());
    }

    @Override
    public Vector3dc vPos(){
        return VSMathUtils.getAbsolutePosition(pos, level, align);
    }


    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof LogicalSpatial so))return false;
        return pos.equals(so.pos);
    }
}
