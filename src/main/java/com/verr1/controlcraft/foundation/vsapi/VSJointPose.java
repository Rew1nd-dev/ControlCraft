package com.verr1.controlcraft.foundation.vsapi;

import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public record VSJointPose(@NotNull Vector3d position, @NotNull Quaterniond rotation) {
    public VSJointPose(Vector3dc position, Quaterniondc rotation) {
        this(new Vector3d(position), new Quaterniond(rotation));
    }

    public Vector3dc getPos(){
        return position;
    }

    public Quaterniondc getRot(){
        return rotation;
    }

}
