package com.verr1.controlcraft.content.gui.legacy.element_;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.layouts.NetworkUIPort;
import com.verr1.controlcraft.content.gui.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class ComponentUIView_ extends NetworkUIPort<Component> implements TitleLabelProvider {

    FormattedLabel title = new FormattedLabel(0, 0, Component.literal(""));
    FormattedLabel view = new FormattedLabel(0, 0, Component.literal(""));

    public ComponentUIView_(Supplier<Component> read, Component titleText) {
        super($ -> {}, read);
        this.title.text = titleText;
        this.title.setWidth(Minecraft.getInstance().font.width(titleText));
    }

    public ComponentUIView_(Supplier<Component> read, LabelProvider titleText) {
        super($ -> {}, read);
        this.title = titleText.toDescriptiveLabel();
    }

    @Override
    protected void initLayout(GridLayout layoutToFill) {
        layoutToFill.addChild(title, 0, 0);
        layoutToFill.addChild(view, 0, 1);
        layoutToFill.rowSpacing(4);
    }

    @Override
    public void onScreenTick() {
        readToLayout();
    }

    @Override
    protected Component readGUI() {
        return view.text;
    }

    @Override
    protected void writeGUI(Component value) {
        view.text = value;
    }

    @Override
    public Label title() {
        return title;
    }
}
