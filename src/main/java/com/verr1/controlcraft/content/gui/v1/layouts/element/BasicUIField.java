package com.verr1.controlcraft.content.gui.v1.layouts.element;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.v1.layouts.NetworkUIPort;
import com.verr1.controlcraft.content.gui.v1.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.v1.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.v1.widgets.FormattedLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BasicUIField<T> extends NetworkUIPort<T> implements TitleLabelProvider {
    protected final FormattedLabel title;
    protected final EditBox field;

    protected final Function<T, String> parseIn;
    protected final Function<String, T> parseOut;

    public BasicUIField(
            Consumer<T> write,
            Supplier<T> read,
            Component titleText,
            Function<T, String> parseIn,
            Function<String, T> parseOut
    ) {
        super(write, read);
        title = new FormattedLabel(0, 0, titleText);
        title.text = titleText;
        this.parseIn = parseIn;
        this.parseOut = parseOut;
        field = new EditBox(Minecraft.getInstance().font, 0, 0, 60, 10, Component.literal(""));
    }

    public BasicUIField(
            Consumer<String> write,
            Supplier<String> read,
            LabelProvider titleText,
            Function<T, String> parseIn,
            Function<String, T> parseOut
    ) {
        super(s -> write.accept(parseIn.apply(s)), () -> parseOut.apply(read.get()));
        title = titleText.toDescriptiveLabel();
        this.parseIn = parseIn;
        this.parseOut = parseOut;
        field = new EditBox(Minecraft.getInstance().font, 0, 0, 60, 10, Component.literal(""));
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
    protected T readGUI() {
        return parseOut.apply(field.getValue());
    }

    @Override
    protected void writeGUI(T value) {
        field.setValue(parseIn.apply(value));
    }

    @Override
    public Label title() {
        return title;
    }
}
