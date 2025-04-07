package com.verr1.controlcraft.content.gui.v2.element;

import com.verr1.controlcraft.content.gui.v1.layouts.api.LabelProvider;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.core.BlockPos;

import java.util.function.Function;

public class DoubleUIField extends BasicUIField<Double>{

    public DoubleUIField(
            BlockPos boundPos,
            NetworkKey key,
            LabelProvider titleProv
    ) {
        super(
                boundPos,
                key,
                Double.class,
                0.0,
                titleProv,
                d -> d + "",
                ParseUtils::tryParseDouble
        );
    }


}
