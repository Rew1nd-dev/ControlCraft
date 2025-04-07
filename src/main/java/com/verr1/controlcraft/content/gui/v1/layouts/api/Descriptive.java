package com.verr1.controlcraft.content.gui.v1.layouts.api;


import com.verr1.controlcraft.content.gui.v1.widgets.FormattedLabel;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Descriptive<T extends Enum<?>> extends ComponentLike {

    T self();

    Class<T> clazz();

    default FormattedLabel toDescriptiveLabel() {
        return toUILabel().withToolTips(specific());
    }

    default @NotNull Component asComponent(){return LangUtils.nameOf(clazz(), self());}

    default List<Component> overall(){return LangUtils.descriptionsOf(clazz());}

    default List<Component> specific() {
        return LangUtils.descriptionsOf(clazz(), self());
    }
}
