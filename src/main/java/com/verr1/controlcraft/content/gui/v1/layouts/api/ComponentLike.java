package com.verr1.controlcraft.content.gui.v1.layouts.api;

import com.verr1.controlcraft.content.gui.v1.widgets.FormattedLabel;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ComponentLike extends LabelProvider {

    @NotNull
    Component asComponent();

    default FormattedLabel toUILabel() {
        var l = new FormattedLabel(0, 0, asComponent());
        l.setText(asComponent());
        return l;
    }

    @Override
    default FormattedLabel toDescriptiveLabel() {
        return toUILabel();
    }

    static @NotNull Component tryAsComponent(@NotNull Enum<?> enumValue) {
        return Optional
                .of(enumValue)
                .filter(ComponentLike.class::isInstance)
                .map(ComponentLike.class::cast)
                .map(ComponentLike::asComponent)
                .orElse(Component.literal(enumValue.name()));
    }

}
