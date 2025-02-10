package com.verr1.vscontrolcraft.base.UltraTerminal;

public enum ExposedFieldType {

    NONE,
    P,
    I,
    D,
    TARGET,

    ANGLE,
    HORIZONTAL_TILT,
    VERTICAL_TILT,
    SPEED,

    TORQUE,
    FORCE,
    THRUST,

    IS_LOCKED,

    OFFSET,
    IS_RUNNING,
    IS_STATIC;


    public boolean isBoolean(){
        return this == IS_LOCKED || this == IS_RUNNING || this == IS_STATIC;
    }

}
