package com.verr1.vscontrolcraft.blocks.spinalyzer;

import com.verr1.vscontrolcraft.utils.CCUtils;
import kotlin.reflect.jvm.internal.impl.util.ModuleVisibilityHelper;
import org.joml.*;

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
