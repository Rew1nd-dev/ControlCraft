package com.verr1.controlcraft.foundation.data.constraint;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.valkyrienskies.core.apigame.joints.VSJointPose;

public record ConnectContext(VSJointPose self, VSJointPose comp) {
    public static ConnectContext EMPTY = new ConnectContext(
            new VSJointPose(new Vector3d(), new Quaterniond()),
            new VSJointPose(new Vector3d(), new Quaterniond())
    );
}
