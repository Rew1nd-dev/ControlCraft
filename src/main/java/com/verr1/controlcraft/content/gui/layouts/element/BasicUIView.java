package com.verr1.controlcraft.content.gui.layouts.element;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class BasicUIView <T> extends TypedUIPort<T>  implements TitleLabelProvider {

    FormattedLabel title;
    FormattedLabel view = new FormattedLabel(0, 0, Component.literal(""));

    protected final Function<T, Component> parseIn;
    protected final Function<Component, T> parseOut;

    public BasicUIView(
            BlockPos boundPos,
            NetworkKey key,
            Class<T> dataType,
            T defaultValue,
            LabelProvider label,
            Function<T, Component> parseIn,
            Function<Component, T> parseOut
    ) {
        super(boundPos, key, dataType, defaultValue);
        this.title = label.toDescriptiveLabel();
        this.parseIn = parseIn;
        this.parseOut = parseOut;
    }


    @Override
    protected void initLayout(GridLayout layoutToFill) {
        layoutToFill.addChild(title, 0, 0);
        layoutToFill.addChild(view, 0, 1);
        layoutToFill.columnSpacing(4);
    }

    @Override
    public void onScreenTick() {
        readToLayout();
    }

    @Override
    protected T readGUI() {
        return null; // parseOut.apply(view.text);
    }

    @Override
    protected void writeGUI(T value) {
        view.setText(parseIn.apply(value));
    }

    @Override
    public Label title() {
        return title;
    }
}
