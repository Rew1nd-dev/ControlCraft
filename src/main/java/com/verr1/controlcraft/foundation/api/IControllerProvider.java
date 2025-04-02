package com.verr1.controlcraft.foundation.api;

import com.verr1.controlcraft.foundation.data.control.DynamicController;
import com.verr1.controlcraft.foundation.data.control.PID;

public interface IControllerProvider {

    PID DEFAULT_POSITION_MODE_PARAMS = new PID(24, 0, 14);
    PID DEFAULT_VELOCITY_MODE_PARAMS = new PID(10, 0, 0);

    DynamicController getController();
}
