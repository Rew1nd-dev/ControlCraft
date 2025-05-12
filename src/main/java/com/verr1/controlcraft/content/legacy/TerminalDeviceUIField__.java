package com.verr1.controlcraft.content.legacy;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.factory.Converter;
import com.verr1.controlcraft.content.gui.widgets.DescriptiveScrollInput;
import com.verr1.controlcraft.content.gui.layouts.element.TypedUIPort;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import com.verr1.controlcraft.foundation.api.delegate.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.type.descriptive.SlotDirection;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import com.verr1.controlcraft.foundation.type.descriptive.MiscDescription;
import com.verr1.controlcraft.foundation.type.descriptive.UIContents;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.verr1.controlcraft.content.gui.factory.Converter.convert;

public class TerminalDeviceUIField__ extends TypedUIPort<CompoundTag> implements ITerminalDevice{

    private int actualSize = 0;
    private final ArrayList<ExposedFieldWrapper> GUIFields = new ArrayList<>(
            List.of(
                    new ExposedFieldWrapper(() -> 0.0, $ -> {}, "GUI_0", SlotType.NONE),
                    new ExposedFieldWrapper(() -> 0.0, $ -> {}, "GUI_1", SlotType.NONE),
                    new ExposedFieldWrapper(() -> 0.0, $ -> {}, "GUI_2", SlotType.NONE),
                    new ExposedFieldWrapper(() -> 0.0, $ -> {}, "GUI_3", SlotType.NONE),
                    new ExposedFieldWrapper(() -> 0.0, $ -> {}, "GUI_4", SlotType.NONE),
                    new ExposedFieldWrapper(() -> 0.0, $ -> {}, "GUI_5", SlotType.NONE)
            )
    );

    public DescriptiveScrollInput<SlotType> fieldSelector;
    public DescriptiveScrollInput<SlotDirection> directionSelector;

    public Label minLabel = convert(UIContents.MIN, Converter::titleStyle).toDescriptiveLabel();
    public Label maxLabel = convert(UIContents.MAX, Converter::titleStyle).toDescriptiveLabel();
    public FormattedLabel fieldLabel;
    public FormattedLabel directionLabel;
    public EditBox minField;
    public EditBox maxField;

    public IconButton confirm;
    public IconButton reset;

    private final GridLayout buttonLayout = new GridLayout();
    private final GridLayout fieldSelectLayout = new GridLayout();
    private final GridLayout directionSelectLayout = new GridLayout();


    public TerminalDeviceUIField__(
            BlockPos boundPos
    ) {
        super(boundPos, ITerminalDevice.FIELD__, CompoundTag.class, new CompoundTag());
        earlyInit();
    }

    private void earlyInit(){
        fieldSelector = new DescriptiveScrollInput<>(0, 0, 10, 10, ControlCraftGuiTextures.SMALL_BUTTON_SELECTION); // see lateInit()
        directionSelector = new DescriptiveScrollInput<>(0, 0, 10, 10, ControlCraftGuiTextures.SMALL_BUTTON_SELECTION, SlotDirection.class);
        fieldLabel = new FormattedLabel(0,0, Component.literal("      "));
        directionLabel = new FormattedLabel(0, 0, Component.literal("      "));
        minField = new EditBox(Minecraft.getInstance().font, 0, 0, 60, 10, Component.empty());
        maxField = new EditBox(Minecraft.getInstance().font, 0, 0, 60, 10, Component.empty());
        confirm = new IconButton(0, 0, AllIcons.I_CONFIRM);
        reset = new IconButton(0, 0, AllIcons.I_TRASH);

        maxField.setEditable(true);
        minField.setEditable(true);
        minField.setFilter(ParseUtils::tryParseDoubleFilter);
        maxField.setFilter(ParseUtils::tryParseDoubleFilter);
        confirm.withCallback(this::confirmGUI);
        // confirm.setToolTip(Components.translatable(ControlCraft.MODID + ".tooltip.confirm_face_settings"));
        reset.getToolTip().clear();
        reset.getToolTip().addAll(MiscDescription.DUMP.specific());
        reset.withCallback(this::resetGUI);
    }

    protected void lateInit(){
        directionSelector.valueCalling(
                it -> directionLabel.text = it.asComponent().copy().withStyle(Converter::optionStyle)
        );
        fieldSelector.withValues(GUIFields.stream().limit(actualSize).map(w -> w.type).toArray(SlotType[]::new));
        fieldSelector
                .calling(i -> {
                    var currentSelection = fields().get(fieldSelector.getState());
                    fieldLabel.text = currentSelection.type.asComponent().copy().withStyle(Converter::optionStyle);
                    directionSelector.setState(currentSelection.directionOptional.ordinal());
                    directionSelector.onChanged();
                    maxField.setValue(String.format("%.2f", currentSelection.min_max.get(false)));
                    minField.setValue(String.format("%.2f", currentSelection.min_max.get(true)));
                    if(isActivated)setVisibility();
                });
        fieldSelector.onChanged();

        setMaxLength();
    }

    // don set AbstractWidget::visible, because tab use it to decide which to show
    private void setVisibility(){
        var currentSelection = fields().get(fieldSelector.getState());
        minField.visible = !currentSelection.type.isBoolean();
        maxField.visible = !currentSelection.type.isBoolean();
        minLabel.visible = !currentSelection.type.isBoolean();
        maxLabel.visible = !currentSelection.type.isBoolean();
    }

    @Override
    public void onActivatedTab() {
        super.onActivatedTab();
    }

    private void setMaxLength(){
        AtomicInteger maxLen = new AtomicInteger(0);
        fieldSelector.values().stream().map(SlotType::asComponent).forEach(c -> {
            int len = Minecraft.getInstance().font.width(c.copy().withStyle(Converter::optionStyle));
            if(len > maxLen.get()) maxLen.set(len);
        });
        directionSelector.values().stream().map(SlotDirection::asComponent).forEach(c -> {
            int len = Minecraft.getInstance().font.width(c.copy().withStyle(Converter::optionStyle));
            if(len > maxLen.get()) maxLen.set(len);
        });
        fieldLabel.setWidth(maxLen.get());
        directionLabel.setWidth(maxLen.get());
    }


    @Override
    protected void initLayout(GridLayout layoutToFill) {
        fieldSelectLayout.addChild(fieldLabel, 0, 0);
        fieldSelectLayout.addChild(fieldSelector, 0, 1);
        directionSelectLayout.addChild(directionSelector, 0, 1);
        directionSelectLayout.addChild(directionLabel, 0, 0);
        buttonLayout.addChild(reset, 0, 0);
        // buttonLayout.addChild(confirm, 0, 0);
        buttonLayout.columnSpacing(4);

        layoutToFill.addChild(buttonLayout, 5, 0, 1, 2);
        layoutToFill.createRowHelper(5).newCellSettings().alignVerticallyBottom();

        layoutToFill.addChild(fieldSelectLayout, 0, 0, 1, 2);
        layoutToFill.addChild(directionSelectLayout, 1, 0, 1, 2);
        layoutToFill.addChild(minLabel, 2, 0);
        layoutToFill.addChild(minField, 2, 1);
        layoutToFill.addChild(maxLabel, 3, 0);
        layoutToFill.addChild(maxField, 3, 1);
        layoutToFill.rowSpacing(2);

    }

    private void confirmGUI(){
        setExposedField(
                fields().get(fieldSelector.getState()).type,
                ParseUtils.tryParseDouble(minField.getValue()),
                ParseUtils.tryParseDouble(maxField.getValue()),
                directionSelector.valueOfOption()
        );
    }

    @Override
    public void onMessage(Message msg) {
        if(msg.equals(Message.PRE_APPLY))confirmGUI();
    }

    private void resetGUI(){
        ITerminalDevice.super.reset();
        directionSelector.onChanged();
        fieldSelector.onChanged();
    }

    @Override
    protected CompoundTag readGUI() {
        return ITerminalDevice.super.serialize();
    }

    @Override
    protected void writeGUI(@NotNull CompoundTag value) {
        actualSize = value.getInt("fields");
        ITerminalDevice.super.deserializeUnchecked(value);
        lateInit();
    }

    @Override
    public List<ExposedFieldWrapper> fields() {
        return GUIFields;
    }

    @Override
    public String name() {
        return "GUIField";
    }
}
