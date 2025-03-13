package com.verr1.controlcraft.foundation.data;

import com.verr1.controlcraft.utils.CCUtils;
import org.joml.*;
import org.valkyrienskies.core.api.ships.PhysShip;

import java.util.Map;

public record ShipPhysics(Vector3dc velocity,
                          Vector3dc omega,
                          Vector3dc position,
                          Quaterniondc quaternion,
                          Matrix3dc inertiaTensor,
                          Matrix3dc rotationMatrix,
                          Matrix4dc s2wTransform,
                          Matrix4dc w2sTransform,
                          double mass,
                          Long ID
){
    public static ShipPhysics EMPTY = new ShipPhysics(
            new Vector3d(),
            new Vector3d(),
            new Vector3d(),
            new Quaterniond(),
            new Matrix3d(),
            new Matrix3d(),
            new Matrix4d(),
            new Matrix4d(),
            0,
            -1L
    );

    public static ShipPhysics of(PhysShip ship){
        return new ShipPhysics(
                        new Vector3d(ship.getVelocity()),
                        new Vector3d(ship.getAngularVelocity()),
                        new Vector3d(ship.getTransform().getPosition()),
                        new Quaterniond(ship.getTransform().getRotation()),
                        new Matrix3d(ship.getMomentOfInertia()),
                        new Matrix3d(ship.getTransform().getShipToWorld()),
                        new Matrix4d(ship.getTransform().getShipToWorld()),
                        new Matrix4d(ship.getTransform().getWorldToShip()),
                        ship.getMass(),
                        ship.getId()
                );
    }


    public Map<String, Object> getCCPhysics(){
        return Map.of(
                "velocity", CCUtils.dumpVec3(velocity()),
                "omega", CCUtils.dumpVec3(omega()),
                "position", CCUtils.dumpVec3(position()),
                "quaternion", CCUtils.dumpVec4(quaternion()),
                "up", CCUtils.dumpVec3(quaternion().transform(new Vector3d(0, 1, 0))),
                "mass", mass(),
                "id", ID()
        );
    }

}
