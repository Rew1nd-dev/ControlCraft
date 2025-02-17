package com.verr1.vscontrolcraft.base.UltraTerminal;

import com.ibm.icu.impl.ICUNotifier;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Components;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.SimpleSettingScreen;
import com.verr1.vscontrolcraft.blocks.terminal.SmallCheckbox;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllGuiLabels;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.registry.AllVSCCGuiTextures;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.impl.shadow.S;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.stream.IntStream;

public class ExposedFieldSettingScreen extends AbstractSimiScreen {

    private final AllVSCCGuiTextures background = AllVSCCGuiTextures.SIMPLE_BACKGROUND_HALF;

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
                        .map(d -> Component.literal(d.name()))
                        .toList()
        ).withRange(0, ExposedFieldDirection.values().length).calling(
            i -> directionLabel.text = ExposedFieldDirection.convert(i).getComponent()
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
                    fieldLabel.text = currentSelection.type().getComponent();
                    directionSelector.setState(currentSelection.openTo().ordinal());
                    directionSelector.onChanged();
                    maxField.setValue(String.format("%.2f", currentSelection.max()));
                    minField.setValue(String.format("%.2f", currentSelection.min()));
                });


        minLabel = new Label(0,0, AllGuiLabels.minLabel);
        maxLabel = new Label(0,0, AllGuiLabels.maxLabel);
        fieldLabel = new Label(0,0, Component.literal("LLLLLLLL"));
        directionLabel = new Label(0, 0, Component.literal("LLLLLLLL"));


        minField = new EditBox(font, 0, 0, 40, 10, Component.empty());
        maxField = new EditBox(font, 0, 0, 40, 10, Component.empty());

        var currentSelection = availableFields.get(fieldSelector.getState());
        maxField.setValue(String.format("%.2f", currentSelection.max()));
        minField.setValue(String.format("%.2f", currentSelection.min()));
        fieldLabel.text = currentSelection.type().getComponent();

        maxField.setEditable(true);
        minField.setEditable(true);

        // fieldLabel.text = Component.literal("field");
        minLabel.text = AllGuiLabels.minLabel;
        maxLabel.text = AllGuiLabels.maxLabel;



        minField.setFilter(Util::tryParseDoubleFilter);


        maxField.setFilter(Util::tryParseDoubleFilter);


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
        AllPackets.getChannel().sendToServer(
                new ExposedFieldResetPacket(
                        pos
                )
        );
    }

    public void confirm(){
        AllPackets.getChannel().sendToServer(
                new ExposedFieldSettingsPacket(
                        pos,
                        availableFields.get(fieldSelector.getState()).type(),
                        Util.tryParseDouble(minField.getValue()),
                        Util.tryParseDouble(maxField.getValue()),
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
        AllVSCCGuiTextures.SMALL_BUTTON_GREEN.render(graphics, fieldSelector.getX(), fieldSelector.getY());
        AllVSCCGuiTextures.SMALL_BUTTON_GREEN.render(graphics, directionSelector.getX(), directionSelector.getY());
        boolean shouldRender = !availableFields.get(fieldSelector.getState()).type().isBoolean();
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
