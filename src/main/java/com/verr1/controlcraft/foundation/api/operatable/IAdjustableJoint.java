package com.verr1.controlcraft.foundation.api.operatable;

import com.verr1.controlcraft.foundation.type.JointLevel;

public interface IAdjustableJoint {

    default void adjust(){setAdjustment(getAdjustment().next());};

    JointLevel getAdjustment();

    void setAdjustment(JointLevel level);
}
