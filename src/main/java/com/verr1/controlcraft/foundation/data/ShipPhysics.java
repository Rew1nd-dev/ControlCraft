package com.verr1.controlcraft.foundation.data;

import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import com.verr1.controlcraft.utils.CCUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import java.util.Map;

public record ShipPhysics(Vector3dc velocity,
                          Vector3dc omega,
                          Vector3dc position,
                          Vector3dc positionInShip,
                          Quaterniondc quaternion,
                          Matrix3dc inertiaTensor,
                          Matrix3dc rotationMatrix,
                          Matrix4dc s2wTransform,
                          Matrix4dc w2sTransform,
                          double mass,
                          double scale,
                          Long ID
){
    public static ShipPhysics EMPTY = new ShipPhysics(
            new Vector3d(),
            new Vector3d(),
            new Vector3d(),
            new Vector3d(),
            new Quaterniond(),
            new Matrix3d(),
            new Matrix3d(),
            new Matrix4d(),
            new Matrix4d(),
            0,
            1,
            -1L
    );

    public static ShipPhysics of(@Nullable PhysShip ship_){
        if(ship_ == null)return EMPTY;
        PhysShipWrapper ship = new PhysShipWrapper((PhysShipImpl) ship_);
        return new ShipPhysics(
                        new Vector3d(ship.getVelocity()),
                        new Vector3d(ship.getAngularVelocity()),
                        new Vector3d(ship.getTransform().getPositionInWorld()),
                        new Vector3d(ship.getTransform().getPositionInShip()),
                        new Quaterniond(ship.getTransform().getShipToWorldRotation()),
                        new Matrix3d(ship.getMomentOfInertia()),
                        new Matrix3d(ship.getTransform().getShipToWorld()),
                        new Matrix4d(ship.getTransform().getShipToWorld()),
                        new Matrix4d(ship.getTransform().getWorldToShip()),
                        ship.getMass(),
                        ship.getTransform().getShipToWorldScaling().get(0),
                        ship.getId()
                );
    }

    public static ShipPhysics of(@Nullable ServerShip ship){
        if(ship == null)return EMPTY;
        return new ShipPhysics(
                new Vector3d(ship.getVelocity()),
                new Vector3d(ship.getOmega()),
                new Vector3d(ship.getTransform().getPositionInWorld()),
                new Vector3d(ship.getTransform().getPositionInShip()),
                new Quaterniond(ship.getTransform().getShipToWorldRotation()),
                new Matrix3d(ship.getInertiaData().getMomentOfInertiaTensor()),
                new Matrix3d(ship.getTransform().getShipToWorld()),
                new Matrix4d(ship.getTransform().getShipToWorld()),
                new Matrix4d(ship.getTransform().getWorldToShip()),
                ship.getInertiaData().getMass(),
                ship.getTransform().getShipToWorldScaling().get(0),
                ship.getId()
        );
    }


    public Map<String, Object> toLua(){
        return Map.of(
                "velocity", CCUtils.dumpVec3(velocity()),
                "omega", CCUtils.dumpVec3(omega()),
                "position", CCUtils.dumpVec3(position()),
                "positionInShip", CCUtils.dumpVec3(positionInShip()),
                "quaternion", CCUtils.dumpVec4(quaternion()),
                "up", CCUtils.dumpVec3(quaternion().transform(new Vector3d(0, 1, 0))),
                "mass", mass(),
                "inertia", inertiaTensor().m00(),
                "id", ID()
        );
    }

}
