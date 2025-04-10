package com.verr1.controlcraft.content.gui.layouts.element;

import com.verr1.controlcraft.content.gui.layouts.api.LabelProvider;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.core.BlockPos;

public class LongUIField extends BasicUIField<Long>{
    public LongUIField(
            BlockPos boundPos,
            NetworkKey key,
            LabelProvider titleProv
    ) {
        super(boundPos, key, Long.class, 0L, titleProv, l -> l + "", ParseUtils::tryParseLong);
    }
}
