package com.verr1.controlcraft.content.gui.layouts.preset;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.blocks.SharedKeys;
import com.verr1.controlcraft.content.gui.factory.Converter;
import com.verr1.controlcraft.content.gui.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import com.verr1.controlcraft.content.gui.layouts.element.TypedUIPort;
import com.verr1.controlcraft.content.gui.layouts.api.ISerializableDynamicController;
import com.verr1.controlcraft.foundation.data.control.PID;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.descriptive.UIContents;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import static com.verr1.controlcraft.content.gui.factory.Converter.convert;

public class DynamicControllerUIField extends TypedUIPort<CompoundTag> implements
        ISerializableDynamicController, TitleLabelProvider
{
    FormattedLabel title =  convert(UIContents.PID_CONTROLLER, Converter::pidStyle).toDescriptiveLabel();
    FormattedLabel pLabel = convert(ExposedFieldType.P, Converter::pidStyle).toDescriptiveLabel();
    FormattedLabel iLabel = convert(ExposedFieldType.I, Converter::pidStyle).toDescriptiveLabel();
    FormattedLabel dLabel = convert(ExposedFieldType.D, Converter::pidStyle).toDescriptiveLabel();
    EditBox pField;
    EditBox iField;
    EditBox dField;

    PID pid = PID.EMPTY;

    public DynamicControllerUIField(
            BlockPos boundPos,
            int fieldLength
    ) {
        super(boundPos, SharedKeys.CONTROLLER, CompoundTag.class, new CompoundTag());
        Font font = Minecraft.getInstance().font;
        pField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        iField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        dField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        Converter.alignLabel(titles());
    }

    @Override
    protected void initLayout(GridLayout layoutToFill) {
        layoutToFill.addChild(title, 0, 0, 1, 5);
        layoutToFill.addChild(pLabel, 1, 0);
        layoutToFill.addChild(pField, 1, 1);
        layoutToFill.addChild(iLabel, 1, 2);
        layoutToFill.addChild(iField, 1, 3);
        layoutToFill.addChild(dLabel, 1, 4);
        layoutToFill.addChild(dField, 1, 5);
        layoutToFill.rowSpacing(4).columnSpacing(2);
    }

    @Override
    protected CompoundTag readGUI() {
        PID(new PID(
                ParseUtils.tryParseDouble(pField.getValue()),
                ParseUtils.tryParseDouble(iField.getValue()),
                ParseUtils.tryParseDouble(dField.getValue())
        ));
        return ISerializableDynamicController.super.serialize();
    }

    @Override
    protected void writeGUI(CompoundTag value) {
        ISerializableDynamicController.super.deserialize(value);
        pField.setValue("" + PID().p());
        iField.setValue("" + PID().i());
        dField.setValue("" + PID().d());
    }

    @Override
    public void PID(PID pid) {
        this.pid = pid;
    }

    @Override
    public PID PID() {
        return pid;
    }

    @Override
    public Label title() {
        return title;
    }

    @Override
    public Label[] titles() {
        return new Label[]{pLabel, iLabel, dLabel};
    }
}
