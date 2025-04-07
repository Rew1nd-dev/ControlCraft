package com.verr1.controlcraft.content.gui.legacy;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Components;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldMessage;
import com.verr1.controlcraft.foundation.network.packets.GenericServerPacket;
import com.verr1.controlcraft.foundation.network.packets.specific.ExposedFieldSettingsPacket;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldDirection;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftGuiLabels;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import java.util.Arrays;
import java.util.List;

public class ExposedFieldSettingScreen extends AbstractSimiScreen {

    private final ControlCraftGuiTextures background = ControlCraftGuiTextures.SIMPLE_BACKGROUND_HALF;

    private final List<ExposedFieldMessage> availableFields;
    private final BlockPos pos;

    private SelectionScrollInput fieldSelector;
    private SelectionScrollInput directionSelector;

    private Label minLabel;
    private Label maxLabel;
    private Label fieldLabel;
    private Label directionLabel;
    private Label directionValueLabel;
    private EditBox minField;
    private EditBox maxField;

    private IconButton confirm;
    private IconButton reset;



    private final GridLayout layout = new GridLayout();
    private final GridLayout buttonLayout = new GridLayout();
    private final GridLayout fieldSelectLayout = new GridLayout();
    private final GridLayout directionSelectLayout = new GridLayout();

    public ExposedFieldSettingScreen(BlockPos pos, List<ExposedFieldMessage> availableFields) {
        this.pos = pos;
        this.availableFields = availableFields;

    }

    private void initWidgets(){
        fieldSelector = new SelectionScrollInput(0, 0, 10, 10);



        directionSelector = new SelectionScrollInput(0, 0, 10, 10);

        directionSelector.forOptions(
                Arrays.stream(ExposedFieldDirection.values())
                        .map(d -> Component.literal(d.asComponent().getString()))
                        .toList()
        ).withRange(0, ExposedFieldDirection.values().length).calling(
                i -> directionLabel.text = ExposedFieldDirection.convert(i).asComponent()
        );

        fieldSelector
                .forOptions(
                        availableFields
                                .stream()
                                .map(f -> Component.literal(f.type().name()))
                                .toList())
                .withRange(0, availableFields.size())
                .setState(0)
                .calling(i -> {
                    var currentSelection = availableFields.get(fieldSelector.getState());
                    fieldLabel.text = currentSelection.type().asComponent();
                    directionSelector.setState(currentSelection.openTo().ordinal());
                    directionSelector.onChanged();
                    maxField.setValue(String.format("%.2f", currentSelection.max()));
                    minField.setValue(String.format("%.2f", currentSelection.min()));
                });


        minLabel = new Label(0,0, ControlCraftGuiLabels.minLabel);
        maxLabel = new Label(0,0, ControlCraftGuiLabels.maxLabel);
        fieldLabel = new Label(0,0, Component.literal("LLLLLLLL"));
        directionLabel = new Label(0, 0, Component.literal("LLLLLLLL"));


        minField = new EditBox(font, 0, 0, 40, 10, Component.empty());
        maxField = new EditBox(font, 0, 0, 40, 10, Component.empty());

        var currentSelection = availableFields.get(fieldSelector.getState());
        maxField.setValue(String.format("%.2f", currentSelection.max()));
        minField.setValue(String.format("%.2f", currentSelection.min()));
        fieldLabel.text = currentSelection.type().asComponent();

        maxField.setEditable(true);
        minField.setEditable(true);

        // fieldLabel.text = Component.literal("field");
        minLabel.text = ControlCraftGuiLabels.minLabel;
        maxLabel.text = ControlCraftGuiLabels.maxLabel;



        minField.setFilter(ParseUtils::tryParseDoubleFilter);


        maxField.setFilter(ParseUtils::tryParseDoubleFilter);


        confirm = new IconButton(0, 0, AllIcons.I_CONFIRM);
        confirm.withCallback(this::confirm);
        confirm.setToolTip(Components.translatable(ControlCraft.MODID + ".tooltip.confirm_face_settings"));
        reset = new IconButton(0, 0, AllIcons.I_TRASH);
        reset.setToolTip(Components.translatable(ControlCraft.MODID + ".tooltip.dump_all_settings"));
        reset.withCallback(this::reset);

        fieldSelectLayout.addChild(fieldLabel, 0, 0);
        fieldSelectLayout.addChild(fieldSelector, 0, 1);

        directionSelectLayout.addChild(directionSelector, 0, 1);
        directionSelectLayout.addChild(directionLabel, 0, 0);

        layout.addChild(fieldSelectLayout, 0, 0, 1, 2);
        layout.addChild(directionSelectLayout, 1, 0, 1, 2);


        buttonLayout.addChild(reset, 0, 1);
        buttonLayout.addChild(confirm, 0, 0);
        buttonLayout.columnSpacing(4);

        layout.addChild(buttonLayout, 5, 0, 1, 2);
        // layout.addChild(reset, 6, 0);

        layout.addChild(minLabel, 2, 0);
        layout.addChild(minField, 2, 1);
        layout.addChild(maxLabel, 3, 0);
        layout.addChild(maxField, 3, 1);
        layout.rowSpacing(2);


        fieldSelector.onChanged(); // update the direction label

        addRenderableWidget(fieldSelector);
        addRenderableWidget(directionSelector);
        addRenderableWidget(directionLabel);
        addRenderableWidget(fieldLabel);
        addRenderableWidget(minLabel);
        addRenderableWidget(minField);
        addRenderableWidget(maxLabel);
        addRenderableWidget(maxField);
        addRenderableWidget(confirm);
        addRenderableWidget(reset);



        // fieldSelector.getState()
    }


    @Override
    public void onClose() {
        super.onClose();
    }

    public void reset(){
        var p = new GenericServerPacket.builder(RegisteredPacketType.GENERIC_RESET_EXPOSED_FIELDS)
                .withLong(pos.asLong())
                .build();

        ControlCraftPackets.getChannel().sendToServer(p);
    }

    public void confirm(){
        ControlCraftPackets.getChannel().sendToServer(
                new ExposedFieldSettingsPacket(
                        pos,
                        availableFields.get(fieldSelector.getState()).type(),
                        ParseUtils.tryParseDouble(minField.getValue()),
                        ParseUtils.tryParseDouble(maxField.getValue()),
                        ExposedFieldDirection.convert(directionSelector.getState())
                )
        );
    }

    @Override
    protected void init() {
        setWindowSize(background.width, background.height);
        super.init();
        initWidgets();
        layout.setX(guiLeft + 4);
        layout.setY(guiTop + 4);
        layout.arrangeElements();
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        background.render(graphics, guiLeft, guiTop);
        ControlCraftGuiTextures.SMALL_BUTTON_GREEN.render(graphics, fieldSelector.getX(), fieldSelector.getY());
        ControlCraftGuiTextures.SMALL_BUTTON_GREEN.render(graphics, directionSelector.getX(), directionSelector.getY());
        boolean shouldRender = !availableFields.get(Math.min(fieldSelector.getState(), availableFields.size())).type().isBoolean();
        minField.active = shouldRender;
        maxField.active = shouldRender;
        minLabel.active = shouldRender;
        maxLabel.active = shouldRender;

        minField.visible = shouldRender;
        maxField.visible = shouldRender;
        minLabel.visible = shouldRender;
        maxLabel.visible = shouldRender;



    }
}

