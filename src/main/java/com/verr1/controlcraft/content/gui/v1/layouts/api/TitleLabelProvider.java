package com.verr1.controlcraft.content.gui.v1.layouts.api;

import com.simibubi.create.foundation.gui.widget.Label;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface TitleLabelProvider {
    Label title();

    default Label[] titles(){return new Label[]{title()};}

    default TitleLabelProvider withTooltip(Component... tooltip){
        title().getToolTip().addAll(List.of(tooltip));
        return this;
    }

    default TitleLabelProvider withTooltip(List<Component> tooltip){
        title().getToolTip().addAll(tooltip);
        return this;
    }
}
