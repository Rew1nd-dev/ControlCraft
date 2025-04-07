package com.verr1.controlcraft.content.gui.v2.element;

import com.verr1.controlcraft.content.gui.v1.layouts.api.LabelProvider;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.core.BlockPos;

import java.util.function.Function;

public class LongUIField extends BasicUIField<Long>{
    public LongUIField(
            BlockPos boundPos,
            NetworkKey key,
            LabelProvider titleProv
    ) {
        super(boundPos, key, Long.class, 0L, titleProv, l -> l + "", ParseUtils::tryParseLong);
    }
}
