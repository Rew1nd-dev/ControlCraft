package com.verr1.controlcraft.utils;

import com.verr1.controlcraft.config.BlockPropertyConfig;

public class InputChecker {



    public static double clampPIDInput(double value) {
        if(BlockPropertyConfig._NO_NEGATIVE_PID_INPUT && value < 0)return -value;
        return value;
    }

}
