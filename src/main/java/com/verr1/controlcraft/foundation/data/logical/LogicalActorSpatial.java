package com.verr1.controlcraft.foundation.data.logical;

import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.foundation.api.ISpatialTarget;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.utils.VSGetterUtils;
import com.verr1.controlcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public record LogicalActorSpatial(
        WorldBlockPos worldBlockPos,
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
    public ServerLevel level() {
        return Optional.ofNullable(ControlCraftServer.INSTANCE).map(worldBlockPos::level).orElse(null);
    }

    @Override
    public BlockPos pos() {
        return worldBlockPos.pos();
    }

    @Override
    public boolean isStatic() {
        return true;
    }



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
    @Override
    public Quaterniondc qBase() {
        Quaterniondc q_ship = VSGetterUtils.getQuaternion(worldBlockPos());
        Function<Vector3dc, Vector3dc> wrapper = v -> ValkyrienSkies.set(new Vector3d(), rotation.apply(new Vec3(v.x(), v.y(), v.z())));
        Vector3dc rx = wrapper.apply(new Vector3d(1, 0, 0));
        Vector3dc ry = wrapper.apply(new Vector3d(0, 1, 0));
        Vector3dc rz = wrapper.apply(new Vector3d(0, 0, 1));
        Matrix3d a2s = new Matrix3d().setColumn(0, rx).setColumn(1, ry).setColumn(2, rz);

        Quaterniondc q_extra = VSMathUtils.m2q(a2s.transpose());
        // ControlCraft.LOGGER.info("q_extra: " + q_extra);
        return q_extra.mul(q_ship, new Quaterniond());
    }



    // Not Using VSGetterUtils::getAbsolutePosition, because it's not a block pos
    @Override
    public Vector3dc vPos() {
        Vector3dc p_sc = ValkyrienSkies.set(new Vector3d(), position);
        return VSGetterUtils
                .getShip(worldBlockPos())
                .map(ship -> ship
                        .getTransform()
                        .getShipToWorld()
                        .transformPosition(p_sc, new Vector3d()))
                .orElse(new Vector3d(p_sc));

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
