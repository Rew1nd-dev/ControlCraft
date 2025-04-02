package com.verr1.controlcraft.foundation.type.descriptive;

import com.verr1.controlcraft.content.gui.v1.layouts.api.Descriptive;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

import static com.verr1.controlcraft.utils.ComponentUtils.literals;

public enum ExposedFieldDirection implements Descriptive<ExposedFieldDirection> {
    UP,
    DOWN,
    NORTH,
    SOUTH,
    EAST,
    WEST,

    NONE(literals("This Field Will Not Be Exposed To Redstone")),
    ALL(literals("This Field Will Be Exposed To All Directions")),;

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

    public boolean test(Direction direction){
        if (this == ALL) return true;
        if (this == NONE) return false;
        return this == convert(direction);
    }

    ExposedFieldDirection(){
        LangUtils.registerDefaultName(ExposedFieldDirection.class, this, Component.literal(name()));
    }

    ExposedFieldDirection(List<Component> description){
        LangUtils.registerDefaultName(ExposedFieldDirection.class, this, Component.literal(name()));
        LangUtils.registerDefaultDescription(ExposedFieldDirection.class, this, description);
    }

    @Override
    public ExposedFieldDirection self() {
        return this;
    }

    @Override
    public Class<ExposedFieldDirection> clazz() {
        return ExposedFieldDirection.class;
    }

    public static void register(){

    }

}


