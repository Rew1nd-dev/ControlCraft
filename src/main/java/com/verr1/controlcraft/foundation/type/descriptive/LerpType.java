package com.verr1.controlcraft.foundation.type.descriptive;

import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Function;

import static com.verr1.controlcraft.utils.ComponentUtils.literals;
import static com.verr1.controlcraft.utils.MathUtils.EXP_1;

public enum LerpType implements Descriptive<LerpType> {
    LINEAR(
            v -> v,
            literals(
                    "Linear Interpolation",
                    "v -> v"
            )
    ),
    RATE_3$2(
            v -> Math.pow(v, 1.5),
            literals(
                    "Power Interpolation",
                    "v -> v ^ 1.5"
            )
    ),
    EXP(
            v -> (Math.exp(v) - 1) / (EXP_1 - 1),
            literals(
                    "Exponent Interpolation",
                    "v -> (e ^ x - 1) / (e - 1)"
            )
    );



    LerpType(
            Function<Double, Double> interpolate,
            List<Component> description
    ) {
        this.interpolate = interpolate;
        LangUtils.registerDefaultName(LerpType.class, this, Component.literal(name().toUpperCase()));
        LangUtils.registerDefaultDescription(LerpType.class, this, description);
    }

    public final Function<Double, Double> interpolate;


    @Override
    public LerpType self() {
        return this;
    }

    @Override
    public Class<LerpType> clazz() {
        return LerpType.class;
    }

    public static void register(){
        LangUtils.registerDefaultDescription(LerpType.class, literals("Decides How To Interpolate for signal / 15"));
    }

}
