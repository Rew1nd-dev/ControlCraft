package com.verr1.controlcraft.content.gui.v1.layouts.element;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.v1.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.v1.layouts.NetworkUIPort;
import com.verr1.controlcraft.content.gui.v1.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.v1.widgets.FormattedLabel;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class DoubleUIView extends NetworkUIPort<Double> implements TitleLabelProvider {


    private final FormattedLabel title;
    private final EditBox field;

    public DoubleUIView(Supplier<Double> read, Component titleText) {
        super($ -> {}, read);
        title = new FormattedLabel(0, 0, titleText);
        title.text = titleText;
        field = new EditBox(Minecraft.getInstance().font, 0, 0, 60, 10, Component.literal(""));
        field.setEditable(false);
    }

    public static DoubleUIView of(Supplier<String> read, Component titleText) {
        return new DoubleUIView(
                () -> ParseUtils.tryParseDouble(read.get()),
                titleText
        );
    }

    public DoubleUIView(Supplier<Double> read, LabelProvider titleText) {
        super($ -> {}, read);
        title = titleText.toDescriptiveLabel();
        field = new EditBox(Minecraft.getInstance().font, 0, 0, 60, 10, Component.literal(""));
        field.setEditable(false);
    }

    public static DoubleUIView of(Supplier<String> read, LabelProvider titleText) {
        return new DoubleUIView(
                () -> ParseUtils.tryParseDouble(read.get()),
                titleText
        );
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

    public EditBox getField(){
        return field;
    }

    @Override
    protected Double readGUI() {
        return ParseUtils.tryParseDouble(field.getValue());
    }

    @Override
    public void onScreenTick() {
        super.onScreenTick();
        readToLayout();
    }

    @Override
    protected void writeGUI(Double value) {
        field.setValue("" + value);
    }

    @Override
    public Label title() {
        return title;
    };
}
