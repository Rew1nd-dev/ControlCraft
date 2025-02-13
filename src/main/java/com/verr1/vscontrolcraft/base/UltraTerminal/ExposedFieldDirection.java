package com.verr1.vscontrolcraft.base.UltraTerminal;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

public enum ExposedFieldDirection {
    UP,
    DOWN,
    NORTH,
    SOUTH,
    EAST,
    WEST,

    NONE,
    ALL;

    public static ExposedFieldDirection convert(Direction direction){
        return switch (direction) {
            case UP -> UP;
            case DOWN -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
        };
    }

    public static ExposedFieldDirection convert(int ordinal){
        if(ordinal < 0 || ordinal >= values().length)return NONE;

        return Arrays.stream(values()).toList().get(ordinal);
    }

    public Component getComponent(){
        return Component.literal(name());
    }

    public boolean test(Direction direction){
        if (this == ALL) return true;
        if (this == NONE) return false;
        return this == convert(direction);
    }

}
