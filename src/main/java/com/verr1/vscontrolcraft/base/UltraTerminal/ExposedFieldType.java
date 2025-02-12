package com.verr1.vscontrolcraft.base.UltraTerminal;

import com.simibubi.create.foundation.utility.Components;
import com.verr1.vscontrolcraft.ControlCraft;
import net.minecraft.network.chat.Component;

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
    IS_STATIC,


    // these are not exposed to the terminal

    STRENGTH,
    AIR_RESISTANCE,
    EXTRA_GRAVITY,

    PROTOCOL,
    NAME,
    TYPE,
    VALUE,

    THRUST_RATIO,
    TORQUE_RATIO;



    public boolean isBoolean(){
        return this == IS_LOCKED || this == IS_RUNNING || this == IS_STATIC;
    }

    public Component getComponent(){

        return Component
                .translatable(ControlCraft.MODID + ".screen.labels.field." + name().toLowerCase());
    }


}
