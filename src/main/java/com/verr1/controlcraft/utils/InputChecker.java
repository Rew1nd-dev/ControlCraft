package com.verr1.controlcraft.utils;

public class InputChecker {

    public static double clampPIDInput(double value) {
        if(value < 0)return -value;
        return value;
    }

}
