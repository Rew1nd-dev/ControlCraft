package com.verr1.controlcraft.foundation.type.descriptive;

import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;

public enum TargetMode implements Descriptive<TargetMode> {
    POSITION,
    VELOCITY,
    ;
    // FORCED_POSITION,
    // FORCED_VELOCITY,
    // POWER;

    TargetMode(){
        LangUtils.registerDefaultName(TargetMode.class, this, Component.literal(name()));
    }

    @Override
    public TargetMode self() {
        return this;
    }

    @Override
    public Class<TargetMode> clazz() {
        return TargetMode.class;
    }

    public static void register(){

    }
}
