package com.verr1.controlcraft.content.gui.v1.layouts.element;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.v1.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.v1.layouts.NetworkUIPort;
import com.verr1.controlcraft.content.gui.v1.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Vector3dUIField extends NetworkUIPort<Vector3dc> implements TitleLabelProvider {

    Label title;
    Label xLabel = new Label(0, 0, Component.literal("x"));
    Label yLabel = new Label(0, 0, Component.literal("y"));
    Label zLabel = new Label(0, 0, Component.literal("z"));
    EditBox xField;
    EditBox yField;
    EditBox zField;

    public Vector3dUIField(Consumer<Vector3dc> write, Supplier<Vector3dc> read, Component titleText, int fieldLength) {
        super(write, read);
        this.title = new Label(0, 0, titleText);
        this.title.text = titleText;
        Font font = Minecraft.getInstance().font;
        xLabel.text = Component.literal("x");
        yLabel.text = Component.literal("y");
        zLabel.text = Component.literal("z");
        xField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        yField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        zField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
    }

    public Vector3dUIField(Consumer<Vector3dc> write, Supplier<Vector3dc> read, LabelProvider titleText, int fieldLength) {
        super(write, read);
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
    protected Vector3dc readGUI() {
        return new Vector3d(
                ParseUtils.tryParseDouble(xField.getValue()),
                ParseUtils.tryParseDouble(yField.getValue()),
                ParseUtils.tryParseDouble(zField.getValue())
        );
    }

    @Override
    protected void writeGUI(Vector3dc value) {
        xField.setValue("" + value.x());
        yField.setValue("" + value.y());
        zField.setValue("" + value.z());
    }

    @Override
    public Label title() {
        return title;
    }
}
