package com.verr1.controlcraft.content.gui.v2.element;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.v1.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.v1.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.v1.widgets.FormattedLabel;
import com.verr1.controlcraft.content.gui.v1.widgets.SmallCheckbox;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public class BooleanUIField extends TypedUIPort<Boolean> implements TitleLabelProvider {

    private final FormattedLabel title;
    private final SmallCheckbox field;

    public BooleanUIField(BlockPos boundPos, NetworkKey key, LabelProvider titleText) {
        super(boundPos, key, Boolean.class, false);
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
