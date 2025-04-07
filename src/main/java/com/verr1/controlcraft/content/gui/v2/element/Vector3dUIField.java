package com.verr1.controlcraft.content.gui.v2.element;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.v1.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.v1.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Vector3dUIField extends TypedUIPort<Vector3d> implements TitleLabelProvider {
    Label title;
    Label xLabel = new Label(0, 0, Component.literal("x"));
    Label yLabel = new Label(0, 0, Component.literal("y"));
    Label zLabel = new Label(0, 0, Component.literal("z"));
    EditBox xField;
    EditBox yField;
    EditBox zField;

    public Vector3dUIField(BlockPos boundPos, NetworkKey key, LabelProvider titleText, int fieldLength){
        super(boundPos, key, Vector3d.class, new Vector3d());
        this.title = titleText.toDescriptiveLabel();
        Font font = Minecraft.getInstance().font;
        xLabel.text = Component.literal("x");
        yLabel.text = Component.literal("y");
        zLabel.text = Component.literal("z");
        xField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        yField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        zField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
    }

    @Override
    protected void initLayout(GridLayout layoutToFill) {
        layoutToFill.addChild(title, 0, 0);
        layoutToFill.addChild(xLabel, 0, 1);
        layoutToFill.addChild(xField, 0, 2);
        layoutToFill.addChild(yLabel, 0, 3);
        layoutToFill.addChild(yField, 0, 4);
        layoutToFill.addChild(zLabel, 0, 5);
        layoutToFill.addChild(zField, 0, 6);
        layoutToFill.rowSpacing(4).columnSpacing(4);
    }

    @Override
    protected Vector3d readGUI() {
        return new Vector3d(
                ParseUtils.tryParseDouble(xField.getValue()),
                ParseUtils.tryParseDouble(yField.getValue()),
                ParseUtils.tryParseDouble(zField.getValue())
        );
    }

    @Override
    protected void writeGUI(Vector3d value) {
        xField.setValue("" + value.x());
        yField.setValue("" + value.y());
        zField.setValue("" + value.z());
    }

    @Override
    public Label title() {
        return title;
    }
}
