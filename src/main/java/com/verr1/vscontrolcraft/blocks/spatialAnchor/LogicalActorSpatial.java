package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public record LogicalActorSpatial(
        ServerLevel level,

        BlockPos pos,
        Direction align,
        Direction forward,
        long shipID,
        String dimensionID,
        long protocol,

        BlockPos orignialBlockPos,
        UnaryOperator<Vec3> rotation,
        Vec3 position
)implements ISpatialTarget {
    @Override
    public boolean isStatic() {
        return true;
    }




    @Override
    public Quaterniondc qBase() {
        Quaterniondc q_ship = VSMathUtils.getQuaternion(new LevelPos(pos, level));
        Function<Vector3dc, Vector3dc> wrapper = v -> Util.Vec3toVector3d(rotation.apply(new Vec3(v.x(), v.y(), v.z())));
        Vector3dc rx = wrapper.apply(new Vector3d(1, 0, 0));
        Vector3dc ry = wrapper.apply(new Vector3d(0, 1, 0));
        Vector3dc rz = wrapper.apply(new Vector3d(0, 0, 1));
        Matrix3d a2s = new Matrix3d().setColumn(0, rx).setColumn(1, ry).setColumn(2, rz);
        /*
         *   Known Issue:
         *   m2q function calculates the extra rotation which caused by rotating contraptions, It returns the same quaternion
         *   of the same rotation matrix
         *
         *   However, In Valkyrie-Skies physics system, a ship rotating by a given axis may have 2 different quaternion even of a
         *   SAME pose, when rotated by 360 degrees, the quaternion will become negative of the previous round
         *
         *   This will cause dynamic spatial anchors do unnecessary extra rotation when trying to align with anchors on a contraption
         *   In Some Angles
         *
         *   I won't try to fix this, so try not to use spatial anchors on rotating contraption,
         *   unless you don't mind the extra rotation :)
         *
         * */
        Quaterniondc q_extra = VSMathUtils.m2q(a2s.transpose());
        // ControlCraft.LOGGER.info("q_extra: " + q_extra);
        return q_extra.mul(q_ship, new Quaterniond());
    }



    // Not Using VSMathUtils::getAbsolutePosition, because it's not a block pos
    @Override
    public Vector3dc vPos() {
        ServerShip ship = VSGameUtilsKt.getShipObjectManagingPos(level, BlockPos.containing(position));
        Vector3dc p_sc = Util.Vec3toVector3d(position);
        if(ship == null)return p_sc;
        return ship.getTransform().getShipToWorld().transformPosition(p_sc, new Vector3d());
    }



    /*
    *   I am not sure, maybe in rare cases when a player put a new moving anchor at the previous position where an anchor was
    *   placed and moved, the new anchor will have the same position as the old anchor, but they should be different
    */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogicalActorSpatial that)) return false;
        return Objects.equals(orignialBlockPos, that.orignialBlockPos);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(orignialBlockPos);
    }
}
