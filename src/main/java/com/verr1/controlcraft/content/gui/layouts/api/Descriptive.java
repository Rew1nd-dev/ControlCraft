package com.verr1.controlcraft.content.gui.layouts.api;


import com.verr1.controlcraft.content.gui.factory.Converter;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.UnaryOperator;

public interface Descriptive<T extends Enum<?>> extends ComponentLike {

    T self();

    Class<T> clazz();

    default FormattedLabel toDescriptiveLabel() {
        return toUILabel().withToolTips(specific());
    }

    default Descriptive<T> convertTo(UnaryOperator<Style> specific,
                                     UnaryOperator<Style> title,
                                     UnaryOperator<Style> overall) {
        return Converter.convert(this, specific, title, overall);
    }

    default @NotNull Component asComponent(){return LangUtils.nameOf(clazz(), self());}

    default List<Component> overall(){return LangUtils.descriptionsOf(clazz());}

    default List<Component> specific() {
        return LangUtils.descriptionsOf(clazz(), self());
    }
}
