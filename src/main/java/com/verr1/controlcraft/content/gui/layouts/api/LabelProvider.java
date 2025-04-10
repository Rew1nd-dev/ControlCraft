package com.verr1.controlcraft.content.gui.layouts.api;

import com.verr1.controlcraft.content.gui.factory.Converter;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import net.minecraft.network.chat.Style;

import java.util.function.UnaryOperator;

public interface LabelProvider {

    FormattedLabel toDescriptiveLabel();

    FormattedLabel toUILabel();

    default LabelProvider convertTo(UnaryOperator<Style> style){
        return Converter.convert(this, style);
    }

}
