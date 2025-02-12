package com.verr1.vscontrolcraft.utils;

import com.verr1.vscontrolcraft.Config;

public class InputChecker {

    public static double clampPIDInput(double v){
        v = Math.abs(v);
        return v;
    }

}
