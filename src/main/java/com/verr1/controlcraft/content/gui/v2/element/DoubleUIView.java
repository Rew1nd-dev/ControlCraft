package com.verr1.controlcraft.content.gui.v2.element;

import com.verr1.controlcraft.content.gui.v1.layouts.api.LabelProvider;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class DoubleUIView extends BasicUIView<Double>{

    public DoubleUIView(
            BlockPos boundPos,
            NetworkKey key,
            LabelProvider label,
            Function<Double, Component> parseIn
    ) {
        super(
                boundPos,
                key,
                Double.class,
                0.0,
                label,
                parseIn,
                s -> 0.0
        );
    }

    public DoubleUIView(
            BlockPos boundPos,
            NetworkKey key,
            LabelProvider label
    ) {
        this(boundPos, key, label, d -> Component.literal(d + ""));
    }

}
