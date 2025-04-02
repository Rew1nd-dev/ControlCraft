package com.verr1.controlcraft.foundation.type.descriptive;

import com.verr1.controlcraft.content.gui.v1.layouts.api.Descriptive;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.verr1.controlcraft.utils.ComponentUtils.literals;
import static com.verr1.controlcraft.utils.LangUtils.descriptionLinesOf;

public enum CheatMode implements Descriptive<CheatMode>
{
    NONE(literals("No Cheats")),
    NO_REPULSE(literals("No Repulse Of Control Torque / Force")),
    ;

    CheatMode() {
    }

    CheatMode(List<Component> description) {
        LangUtils.registerDefaultName(CheatMode.class, this, Component.literal(name()));
        LangUtils.registerDefaultDescription(CheatMode.class, this, description);
    }

    @Override
    public CheatMode self() {
        return this;
    }

    @Override
    public Class<CheatMode> clazz() {
        return CheatMode.class;
    }

    public static void register(){
        LangUtils.registerDefaultDescription(CheatMode.class, literals("Some Convenience"));
        // load by class loader and constructors will call registerDefaultName etc
    }

}
