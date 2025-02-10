package com.verr1.vscontrolcraft.base.Servo;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldRequestPacket;
import com.verr1.vscontrolcraft.registry.AllVSCCGuiTextures;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class PIDControllerScreen extends AbstractSimiScreen {
    private final Component confirmLabel = Lang.text("Set Params").component();

    private final Component cycleLabel = Lang.text("Cycle Mode If Applicable").component();

    private final AllVSCCGuiTextures background = AllVSCCGuiTextures.SIMPLE_BACKGROUND;


    private EditBox pField;
    private EditBox dField;
    private EditBox iField;
    private EditBox vField;
    private EditBox tField;

    private EditBox pLabel;
    private EditBox dLabel;
    private EditBox iLabel;
    private EditBox vLabel;
    private EditBox tLabel;

    private IconButton register;
    private IconButton cycleMode;

    private IconButton redstoneSettings;

    private final double i;
    private final double p;
    private final double d;
    private final double v;
    private final double t;
    private final PIDControllerType type;
    private final BlockPos pos;

    public PIDControllerScreen(BlockPos entityPos, double p, double i, double d, double v, double t, PIDControllerType type) {
        pos = entityPos;
        this.p = p;
        this.i = i;
        this.d = d;
        this.v = v;
        this.t = t;
        this.type = type;
    }


    public void startWindow(){
        setWindowSize(background.width, background.height);
        super.init();
        initWidgets();


        //layout.rowSpacing(4).columnSpacing(1);

        GridLayout totalLayout = new GridLayout(background.width, background.height);

        totalLayout.setX(this.guiLeft + 4);
        totalLayout.setY(this.guiTop + 4);



        //layout.addChild(vField, 0, 4);
        //layout.addChild(vLabel, 0, 3);
        //layout.addChild(register, 5, 0);
        //layout.addChild(cycleMode, 6, 0);

        GridLayout buttonLayout = new GridLayout(0, 0);
        buttonLayout.addChild(cycleMode, 0, 1);
        buttonLayout.addChild(register, 0, 0);
        buttonLayout.addChild(redstoneSettings, 0, 2);
        buttonLayout.columnSpacing(5);

        GridLayout controlValueLayout = new GridLayout(0, 0);
        controlValueLayout.addChild(tLabel, 0, 0);
        controlValueLayout.addChild(tField, 0, 1);
        controlValueLayout.addChild(pLabel, 1, 0);
        controlValueLayout.addChild(pField, 1, 1);
        controlValueLayout.addChild(iLabel, 2, 0);
        controlValueLayout.addChild(iField, 2, 1);
        controlValueLayout.addChild(dLabel, 3, 0);
        controlValueLayout.addChild(dField, 3, 1);
        controlValueLayout.rowSpacing(6);

        GridLayout statisticsLayout = new GridLayout(0, 0);
        statisticsLayout.addChild(vField, 0, 1);
        statisticsLayout.addChild(vLabel, 0, 0);


        // buttonLayout.defaultChildLayoutSetting().alignHorizontallyCenter().paddingLeft(13);
        totalLayout.addChild(statisticsLayout, 0, 1);
        totalLayout.addChild(controlValueLayout, 0, 0);
        totalLayout.addChild(buttonLayout, 2, 0);

        totalLayout.rowSpacing(7).columnSpacing(18);

        totalLayout.arrangeElements();
    }

    public void initWidgets(){

        int common_input_width = 40;
        int common_input_height = 10;
        int common_label_width = 40;
        int common_label_height = 10;

        int common_label_color = new Color(250, 250, 180).getRGB();

        vField = new EditBox(font, 0, 0, common_input_width, common_input_height, Components.literal("Current Value"));
        vField.setTextColor(-1);
        vField.setTextColorUneditable(-1);
        vField.setBordered(false);
        vField.setMaxLength(35);
        vField.setValue(String.format("%.2f", v));
        vField.setEditable(false);
        vField.setFilter(Util::tryParseDoubleFilter);

        addRenderableWidget(vField);

        tField = new EditBox(font, 0, 0, common_input_width, common_input_height, Components.literal("Target Value"));
        tField.setTextColor(-1);
        tField.setTextColorUneditable(-1);
        tField.setBordered(true);
        tField.setMaxLength(35);
        tField.setValue(String.format("%.2f", t));
        tField.setFilter(Util::tryParseDoubleFilter);

        addRenderableWidget(tField);

        iField = new EditBox(font, 0, 0, common_input_width, common_input_height, Components.literal("Integral Ratio"));
        iField.setTextColor(-1);
        iField.setTextColorUneditable(-1);
        iField.setBordered(true);
        iField.setMaxLength(35);
        iField.setFocused(true);

        iField.setValue(String.format("%.2f", i));
        iField.setFilter(Util::tryParseDoubleFilter);

        setFocused(iField);
        addRenderableWidget(iField);

        pField = new EditBox(font, 0, 0, common_input_width, common_input_height, Components.literal("Proportional Ratio"));
        pField.setTextColor(-1);
        pField.setTextColorUneditable(-1);
        pField.setBordered(true);
        pField.setMaxLength(35);
        pField.setValue(String.format("%.2f", p));
        pField.setFilter(Util::tryParseDoubleFilter);

        addRenderableWidget(pField);

        dField = new EditBox(font, 0, 0, common_input_width, common_input_height, Components.literal("Derivative Ratio"));
        dField.setTextColor(-1);
        dField.setTextColorUneditable(-1);
        dField.setBordered(true);

        dField.setMaxLength(35);
        dField.setValue(String.format("%.2f", d));
        dField.setFilter(Util::tryParseDoubleFilter);

        addRenderableWidget(dField);

        register = new IconButton(0, 0, AllIcons.I_CONFIRM);
        register.withCallback(() -> {
            register();
            onClose();
        });
        register.setToolTip(confirmLabel);
        addRenderableWidget(register);

        cycleMode = new IconButton(0, 0, AllIcons.I_CART_ROTATE);
        cycleMode.withCallback(() -> {
            cycle();
            onClose();
        });
        cycleMode.setToolTip(cycleLabel);

        addRenderableWidget(cycleMode);

        redstoneSettings = new IconButton(0, 0, AllIcons.I_ACTIVE);
        redstoneSettings.withCallback(this::redstoneSetting);
        addRenderableWidget(redstoneSettings);



        if(type == PIDControllerType.SLIDER)cycleMode.visible = false;


        pLabel = new EditBox(font, 0, 0, common_label_width,common_label_height, Components.literal(""));
        pLabel.setTextColorUneditable(common_label_color);
        pLabel.setEditable(false);
        pLabel.setBordered(false);
        pLabel.setValue("P:");
        addRenderableWidget(pLabel);

        iLabel = new EditBox(font, 0, 0, common_label_width,common_label_height, Components.literal(""));
        iLabel.setTextColorUneditable(common_label_color);
        iLabel.setEditable(false);
        iLabel.setBordered(false);
        iLabel.setValue("I:");
        addRenderableWidget(iLabel);

        dLabel = new EditBox(font, 0, 0, common_label_width,common_label_height, Components.literal(""));
        dLabel.setTextColorUneditable(common_label_color);
        dLabel.setEditable(false);
        dLabel.setBordered(false);
        dLabel.setValue("D:");
        addRenderableWidget(dLabel);

        tLabel = new EditBox(font, 0, 0, common_label_width,common_label_height, Components.literal(""));
        tLabel.setTextColorUneditable(common_label_color);
        tLabel.setEditable(false);
        tLabel.setBordered(false);
        tLabel.setValue("Target:");
        addRenderableWidget(tLabel);

        vLabel = new EditBox(font, 0, 0, common_label_width,common_label_height, Components.literal(""));
        vLabel.setTextColorUneditable(common_label_color);
        vLabel.setEditable(false);
        vLabel.setBordered(false);
        vLabel.setValue("value:");
        addRenderableWidget(vLabel);

    }

    public void redstoneSetting(){
        AllPackets.getChannel().sendToServer(
                new ExposedFieldRequestPacket(
                        pos
                )
        );
    }

    @Override
    public void init(){
        startWindow();
    }


    @Override
    public void tick(){
        super.tick();
    }


    public void cycle(){
        AllPackets
                .getChannel()
                .sendToServer(
                        new PIDControllerCycleModePacket(
                                pos
                        )
                );
    }

    public void register() {
        AllPackets
                .getChannel()
                .sendToServer(
                        new PIDControllerSettingsPacket(
                                Util.tryParseDouble(pField.getValue()),
                                Util.tryParseDouble(iField.getValue()),
                                Util.tryParseDouble(dField.getValue()),
                                Util.tryParseDouble(tField.getValue()),
                                pos
                        )
                );
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft + 30;
        int y = guiTop + background.height;

        background.render(graphics, guiLeft, guiTop);
        graphics.drawCenteredString(font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);



        GuiGameElement.of(type.asItem()).scale(3)
                .at(x, y, 0)
                .render(graphics);


    }
}
