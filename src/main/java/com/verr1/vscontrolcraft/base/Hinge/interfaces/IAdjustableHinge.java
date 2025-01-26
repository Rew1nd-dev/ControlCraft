package com.verr1.vscontrolcraft.base.Hinge.interfaces;

import com.verr1.vscontrolcraft.base.Hinge.HingeAdjustLevel;

public interface IAdjustableHinge {

    void adjust();

    HingeAdjustLevel getAdjustment();

    void setAdjustment(HingeAdjustLevel level);


}
