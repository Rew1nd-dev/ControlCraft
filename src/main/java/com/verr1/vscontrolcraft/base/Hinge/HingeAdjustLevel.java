package com.verr1.vscontrolcraft.base.Hinge;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum HingeAdjustLevel implements StringRepresentable {
    BASE,
    HALF,
    THREE_QUARTER,
    FULL;

    public HingeAdjustLevel next(){
        return values()[(ordinal() + 1) % values().length];
    }

    public HingeAdjustLevel previous(){
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    public double correspondLength(){
        return switch (this) {
            case BASE -> 0.25;
            case HALF -> 0.5;
            case THREE_QUARTER -> 0.75;
            case FULL -> 1;
        };
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }
}
