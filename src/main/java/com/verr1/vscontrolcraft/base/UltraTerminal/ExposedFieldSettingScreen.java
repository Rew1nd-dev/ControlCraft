package com.verr1.vscontrolcraft.base.UltraTerminal;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import com.verr1.vscontrolcraft.base.SimpleSettingScreen;
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
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.stream.IntStream;

public class ExposedFieldSettingScreen extends AbstractSimiScreen {

    private final AllVSCCGuiTextures background = AllVSCCGuiTextures.SIMPLE_BACKGROUND_HALF;

    private final List<ExposedFieldMessage> availableFields;
    private final ExposedFieldType currentField;
    private final BlockPos pos;

    private SelectionScrollInput fieldSelector;
    private Label minLabel;
    private Label maxLabel;
    private Label fieldLabel;
    private EditBox minField;
    private EditBox maxField;

    private final GridLayout layout = new GridLayout();

    public ExposedFieldSettingScreen(BlockPos pos, List<ExposedFieldMessage> availableFields, ExposedFieldType currentField) {
        this.pos = pos;
        this.availableFields = availableFields;
        this.currentField = currentField;
    }

    private void initWidgets(){
        fieldSelector = new SelectionScrollInput(0, 0, 16, 16);

        fieldSelector
                .forOptions(
                    availableFields
                        .stream()
                        .map(f -> Component.literal(f.type().name()))
                        .toList())
                .withRange(0, availableFields.size())
                .setState(
                    IntStream.range(0, availableFields.size())
                            .filter(i -> availableFields.get(i).type() == currentField)
                            .findFirst()
                            .orElse(0)
                )
                .calling(i -> {
                    var currentSelection = availableFields.get(fieldSelector.getState());
                    fieldLabel.text = currentSelection.type().getComponent();
                    maxField.setValue(String.format("%.2f", currentSelection.max()));
                    minField.setValue(String.format("%.2f", currentSelection.min()));
                });

        minLabel = new Label(0,0, AllGuiLabels.minLabel);
        maxLabel = new Label(0,0, AllGuiLabels.maxLabel);
        fieldLabel = new Label(0,0, AllGuiLabels.fieldLabel);


        minField = new EditBox(font, 0, 0, 55, 10, Component.empty());
        maxField = new EditBox(font, 0, 0, 55, 10, Component.empty());

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
        layout.addChild(fieldLabel, 0, 0);
        layout.addChild(fieldSelector, 4, 0);


        layout.addChild(minLabel, 2, 0);
        layout.addChild(minField, 2, 1);
        layout.addChild(maxLabel, 3, 0);
        layout.addChild(maxField, 3, 1);

        addRenderableWidget(fieldSelector);
        addRenderableWidget(fieldLabel);
        addRenderableWidget(minLabel);
        addRenderableWidget(minField);
        addRenderableWidget(maxLabel);
        addRenderableWidget(maxField);



        // fieldSelector.getState()
    }

    @Override
    public void onClose() {
        super.onClose();
        AllPackets.getChannel().sendToServer(
                new ExposedFieldSettingsPacket(
                        pos,
                        availableFields.get(fieldSelector.getState()).type(),
                        Util.tryParseDouble(minField.getValue()),
                        Util.tryParseDouble(maxField.getValue())
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
        AllIcons.I_REFRESH.render(graphics, fieldSelector.getX(), fieldSelector.getY());
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
