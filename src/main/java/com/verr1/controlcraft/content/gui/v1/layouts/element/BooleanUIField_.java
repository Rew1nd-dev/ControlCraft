package com.verr1.controlcraft.content.gui.v1.layouts.element;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.v1.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.v1.layouts.NetworkUIPort;
import com.verr1.controlcraft.content.gui.v1.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.v1.widgets.FormattedLabel;
import com.verr1.controlcraft.content.gui.v1.widgets.SmallCheckbox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BooleanUIField_ extends NetworkUIPort<Boolean> implements TitleLabelProvider {
    private final FormattedLabel title;
    private final SmallCheckbox field;

    public BooleanUIField_(Consumer<Boolean> write, Supplier<Boolean> read, Component titleText) {
        super(write, read);
        title = new FormattedLabel(0, 0, titleText);
        title.text = titleText;
        field = new SmallCheckbox(0, 0, 60, 10, Component.literal(""), false);
    }

    public BooleanUIField_(Consumer<Boolean> write, Supplier<Boolean> read, LabelProvider titleText) {
        super(write, read);
        title = titleText.toDescriptiveLabel();
        field = new SmallCheckbox(0, 0, 60, 10, Component.literal(""), false);
    }

    @Override
    protected void initLayout(GridLayout gridLayout){
        gridLayout.addChild(title, 0, 0);
        gridLayout.addChild(field, 0, 1);
        gridLayout.rowSpacing(4);

    }


    public Label getLabel(){
        return title;
    }

    public SmallCheckbox getField(){
        return field;
    }

    @Override
    protected Boolean readGUI() {
        return field.selected();
    }

    @Override
    protected void writeGUI(Boolean value) {
        field.setSelected(value);
    }

    @Override
    public Label title() {
        return title;
    }
}
