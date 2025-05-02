package com.verr1.controlcraft.foundation.type.descriptive;

import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.verr1.controlcraft.utils.ComponentUtils.literals;

public enum LockMode implements Descriptive<LockMode> {
    // lock when no target (velocity) or error < 1e-3 (position)
    ON(literals("Auto Lock When:", " .Target Speed = 0", " .Target Angle Reached")),
    OFF(literals("No Lock")),
    ;

    LockMode(List<Component> descriptions){
        LangUtils.registerDefaultName(LockMode.class, this, Component.literal(name()));
        LangUtils.registerDefaultDescription(LockMode.class, this, descriptions);
    }

    @Override
    public @NotNull Component asComponent() {
        return Component.literal(this.name());
    }

    @Override
    public LockMode self() {
        return this;
    }

    @Override
    public Class<LockMode> clazz() {
        return LockMode.class;
    }

    public static void register(){
        LangUtils.registerDefaultDescription(LockMode.class, literals("Clever Locking"));
        // load by class loader and constructors will call registerDefaultName etc
    }
}
