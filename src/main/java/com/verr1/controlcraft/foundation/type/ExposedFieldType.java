package com.verr1.controlcraft.foundation.type;

import com.verr1.controlcraft.ControlCraft;
import net.minecraft.network.chat.Component;

public enum ExposedFieldType {

    NONE(false),
    P(false),
    I(false),
    D(false),

    TARGET(false),
    TARGET$1(false),
    TARGET$2(false),

    ANGLE(false),

    DEGREE(false),
    DEGREE$1(false),
    DEGREE$2(false),

    HORIZONTAL_TILT(false),
    VERTICAL_TILT(false),

    HORIZONTAL_TILT$1(false),
    VERTICAL_TILT$1(false),

    SPEED(false),
    SPEED$1(false),

    TORQUE(false),
    FORCE(false),
    THRUST(false),

    IS_LOCKED(true),
    IS_LOCKED$1(true),

    OFFSET(false),
    IS_RUNNING(true),
    IS_STATIC(true),

    IS_SENSOR(true),

    // these are not exposed to the terminal

    STRENGTH(false),
    AIR_RESISTANCE(false),
    EXTRA_GRAVITY(false),
    ROTATIONAL_RESISTANCE(false),

    PROTOCOL(false),
    NAME(false),
    TYPE(false),
    VALUE(false),

    MODE_ANGULAR(false),
    MODE_POSITION(false),
    MODE_SPEED(false),

    MODE_CHEAT(false),

    THRUST_RATIO(false),
    TORQUE_RATIO(false);

    private boolean isBoolean = false;

    ExposedFieldType(boolean isBoolean) {
        this.isBoolean = isBoolean;
    }

    public boolean isBoolean(){
        return isBoolean;
    }

    public Component getComponent(){
        String[] main_key = name().toLowerCase().split("\\$");
        String main = Component
                .translatable(ControlCraft.MODID + ".screen.labels.field." + main_key[0]).getString();

        if(main_key.length == 1)return Component.literal(main);

        String combined = main; // + "_" + main_key[1]

        return Component.literal(combined);
    }


}
