package com.verr1.controlcraft.foundation.type;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum JointLevel implements StringRepresentable {
    BASE(0.25),
    HALF(0.5),
    THREE_QUARTER(0.75),
    FULL(1);

    private final double height;

    JointLevel(double height){
        this.height = height;
    }

    public JointLevel next(){
        return values()[(ordinal() + 1) % values().length];
    }

    public JointLevel previous(){
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    public double length(){
        return height;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }
}
