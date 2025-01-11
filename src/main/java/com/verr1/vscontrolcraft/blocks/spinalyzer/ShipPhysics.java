package com.verr1.vscontrolcraft.blocks.spinalyzer;

import com.verr1.vscontrolcraft.utils.CCUtils;
import org.joml.Matrix3dc;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Map;

public record ShipPhysics(Vector3dc velocity,
                          Vector3dc omega,
                          Vector3dc position,
                          Quaterniondc quaternion,
                          Matrix3dc inertiaTensor,
                          double mass
){
    public Map<String, Object> getCCPhysics(){
        return Map.of(
                "velocity", CCUtils.dumpVec3(velocity()),
                "omega", CCUtils.dumpVec3(omega()),
                "position", CCUtils.dumpVec3(position()),
                "quaternion", CCUtils.dumpVec4(quaternion()),
                "up", CCUtils.dumpVec3(quaternion().transform(new Vector3d(0, 1, 0)))
        );
    }

}
