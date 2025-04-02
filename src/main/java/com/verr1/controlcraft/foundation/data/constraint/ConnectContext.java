package com.verr1.controlcraft.foundation.data.constraint;

import com.verr1.controlcraft.foundation.vsapi.VSJointPose;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public record ConnectContext(VSJointPose self, VSJointPose comp, boolean isDirty) {


    public static ConnectContext EMPTY = new ConnectContext(
            new VSJointPose(new Vector3d(), new Quaterniond()),
            new VSJointPose(new Vector3d(), new Quaterniond()),
            true
    );
}
