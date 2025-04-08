package com.verr1.controlcraft.content.gui.v1.layouts.preset;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.v1.factory.GenericUIFactory;
import com.verr1.controlcraft.content.gui.v1.layouts.NetworkUIPort;
import com.verr1.controlcraft.content.gui.v1.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.v1.widgets.FormattedLabel;
import com.verr1.controlcraft.foundation.api.IScheduleProvider;
import com.verr1.controlcraft.foundation.api.ISerializableSchedule;
import com.verr1.controlcraft.foundation.data.control.PID;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.descriptive.UIContents;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpatialScheduleUIField_ extends NetworkUIPort<CompoundTag> implements
        ISerializableSchedule, TitleLabelProvider {

    PID qpid = new PID(0, 0, 0);
    PID ppid = new PID(0, 0, 0);

    FormattedLabel title = UIContents.PID_CONTROLLER.toDescriptiveLabel();

    FormattedLabel qTitle = UIContents.QPID_CONTROLLER.toDescriptiveLabel();
    FormattedLabel qpLabel = ExposedFieldType.P.toDescriptiveLabel();
    FormattedLabel qiLabel = ExposedFieldType.I.toDescriptiveLabel();
    FormattedLabel qdLabel = ExposedFieldType.D.toDescriptiveLabel();
    EditBox qpField;
    EditBox qiField;
    EditBox qdField;

    FormattedLabel pTitle = UIContents.PPID_CONTROLLER.toDescriptiveLabel();
    FormattedLabel ppLabel = ExposedFieldType.P.toDescriptiveLabel();
    FormattedLabel piLabel = ExposedFieldType.I.toDescriptiveLabel();
    FormattedLabel pdLabel = ExposedFieldType.D.toDescriptiveLabel();
    EditBox ppField;
    EditBox piField;
    EditBox pdField;

    public SpatialScheduleUIField_(Consumer<CompoundTag> write, Supplier<CompoundTag> read, int fieldLength) {
        super(write, read);
        Font font = Minecraft.getInstance().font;
        ppField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        piField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        pdField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        qpField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        qiField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        qdField = new EditBox(font, 0, 0, fieldLength, 10, Component.literal(""));
        GenericUIFactory.alignLabel(titles());
    }


    public SpatialScheduleUIField_(Supplier<IScheduleProvider> supplier){
        this(
                t -> Optional.ofNullable(supplier.get()).ifPresent(it -> it.getSchedule().deserialize(t)),
                () -> Optional.ofNullable(supplier.get()).map(it -> it.getSchedule().serialize()).orElse(new CompoundTag()),
                30
        );
    }

    @Override
    protected void initLayout(GridLayout layoutToFill) {
        layoutToFill.addChild(title, 0, 0, 1, 5);

        layoutToFill.addChild(qTitle, 1, 0, 1, 5);

        layoutToFill.addChild(qpLabel, 2, 0);
        layoutToFill.addChild(qpField, 2, 1);
        layoutToFill.addChild(qiLabel, 2, 2);
        layoutToFill.addChild(qiField, 2, 3);
        layoutToFill.addChild(qdLabel, 2, 4);
        layoutToFill.addChild(qdField, 2, 5);

        layoutToFill.addChild(pTitle, 3, 0, 1, 5);

        layoutToFill.addChild(ppLabel, 4, 0);
        layoutToFill.addChild(ppField, 4, 1);
        layoutToFill.addChild(piLabel, 4, 2);
        layoutToFill.addChild(piField, 4, 3);
        layoutToFill.addChild(pdLabel, 4, 4);
        layoutToFill.addChild(pdField, 4, 5);

        layoutToFill.rowSpacing(4).columnSpacing(4);

    }

    @Override
    protected CompoundTag readGUI() {
        PPID(new PID(
                ParseUtils.tryParseDouble(ppField.getValue()),
                ParseUtils.tryParseDouble(piField.getValue()),
                ParseUtils.tryParseDouble(pdField.getValue())
        ));

        QPID(new PID(
                ParseUtils.tryParseDouble(qpField.getValue()),
                ParseUtils.tryParseDouble(qiField.getValue()),
                ParseUtils.tryParseDouble(qdField.getValue())
        ));

        return ISerializableSchedule.super.serialize();
    }

    @Override
    protected void writeGUI(CompoundTag value) {
        ISerializableSchedule.super.deserialize(value);
        qpField.setValue("" + QPID().p());
        qiField.setValue("" + QPID().i());
        qdField.setValue("" + QPID().d());
        ppField.setValue("" + PPID().p());
        piField.setValue("" + PPID().i());
        pdField.setValue("" + PPID().d());
    }

    @Override
    public Label title() {
        return title;
    }

    @Override
    public PID QPID() {
        return qpid;
    }

    @Override
    public PID PPID() {
        return ppid;
    }

    @Override
    public void QPID(PID pid) {
        qpid = pid;
    }

    @Override
    public void PPID(PID pid) {
        ppid = pid;
    }

    @Override
    public Label[] titles() {
        return new Label[]{qdLabel, qiLabel, qpLabel, pdLabel, piLabel, ppLabel};
    }
}
