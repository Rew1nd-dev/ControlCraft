package com.verr1.controlcraft.content.gui.v2.element;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.v1.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.v1.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.v1.widgets.FormattedLabel;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class BasicUIField<T> extends TypedUIPort<T> implements TitleLabelProvider {

    protected final FormattedLabel title;
    protected final EditBox field;

    protected final Function<T, String> parseIn;
    protected final Function<String, T> parseOut;

    public BasicUIField(
            BlockPos boundPos,
            NetworkKey key,
            Class<T> dataType,
            T defaultValue,
            LabelProvider titleProv,
            Function<T, String> parseIn,
            Function<String, T> parseOut
    ) {
        super(boundPos, key, dataType, defaultValue);
        this.title = titleProv.toDescriptiveLabel();
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
    protected void writeGUI(@Nullable T value) {
        field.setValue(parseIn.apply(value));
    }

    @Override
    public Label title() {
        return title;
    }
}
