package com.verr1.vscontrolcraft.base.Servo;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldRequestPacket;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldType;
import com.verr1.vscontrolcraft.registry.AllGuiLabels;
import com.verr1.vscontrolcraft.registry.AllVSCCGuiTextures;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

import static com.verr1.vscontrolcraft.registry.AllGuiLabels.*;
import static net.minecraft.ChatFormatting.GRAY;

public class PIDControllerScreen extends AbstractSimiScreen {

    protected final AllVSCCGuiTextures background = AllVSCCGuiTextures.SIMPLE_BACKGROUND;



    protected EditBox pField;
    protected EditBox dField;
    protected EditBox iField;
    protected EditBox vField;
    protected EditBox tField;

    protected Label pLabel;
    protected Label dLabel;
    protected Label iLabel;
    protected Label vLabel;
    protected Label tLabel;

    protected IconButton register;
    protected IconButton cycleMode;

    protected IconButton redstoneSettings;

    protected final double i;
    protected final double p;
    protected final double d;
    protected final double v;
    protected final double t;
    protected final BlockPos pos;

    protected int common_input_width = 40;
    protected int common_input_height = 10;
    protected int common_label_width = 50;
    protected int common_label_height = 10;

    protected int common_label_color = new Color(250, 250, 180).getRGB();

    protected GridLayout totalLayout = new GridLayout(background.width, background.height);
    protected GridLayout buttonLayout = new GridLayout(0, 0);
    protected GridLayout controlValueLayout = new GridLayout(0, 0);
    protected GridLayout statisticsLayout = new GridLayout(0, 0);

    public PIDControllerScreen(BlockPos entityPos, double p, double i, double d, double v, double t) {
        pos = entityPos;
        this.p = p;
        this.i = i;
        this.d = d;
        this.v = v;
        this.t = t;
    }


    public void startWindow(){
        setWindowSize(background.width, background.height);
        super.init();
        initWidgets();


        //layout.rowSpacing(4).columnSpacing(1);



        totalLayout.setX(this.guiLeft + 4);
        totalLayout.setY(this.guiTop + 4);



        //layout.addChild(vField, 0, 4);
        //layout.addChild(vLabel, 0, 3);
        //layout.addChild(register, 5, 0);
        //layout.addChild(cycleMode, 6, 0);


        buttonLayout.addChild(cycleMode, 0, 2);
        buttonLayout.addChild(register, 0, 0);
        buttonLayout.addChild(redstoneSettings, 0, 1);
        buttonLayout.columnSpacing(5);


        controlValueLayout.addChild(tLabel, 0, 0);
        controlValueLayout.addChild(tField, 0, 1);
        controlValueLayout.addChild(pLabel, 1, 0);
        controlValueLayout.addChild(pField, 1, 1);
        controlValueLayout.addChild(iLabel, 2, 0);
        controlValueLayout.addChild(iField, 2, 1);
        controlValueLayout.addChild(dLabel, 3, 0);
        controlValueLayout.addChild(dField, 3, 1);
        controlValueLayout.rowSpacing(2).columnSpacing(3);


        statisticsLayout.addChild(vField, 0, 1);
        statisticsLayout.addChild(vLabel, 0, 0);
        statisticsLayout.columnSpacing(3);


        // buttonLayout.defaultChildLayoutSetting().alignHorizontallyCenter().paddingLeft(13);
        totalLayout.addChild(statisticsLayout, 0, 1);
        totalLayout.addChild(controlValueLayout, 0, 0);
        totalLayout.addChild(buttonLayout, 2, 0);

        totalLayout.rowSpacing(2).columnSpacing(12);

        totalLayout.arrangeElements();
    }

    public void initWidgets(){



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



        pLabel = new Label(0, 0, ExposedFieldType.P.getComponent()).colored(common_label_color);
        pLabel.text = ExposedFieldType.P.getComponent();

        addRenderableWidget(pLabel);

        iLabel = new Label(0, 0, ExposedFieldType.I.getComponent()).colored(common_label_color);
        iLabel.text = ExposedFieldType.I.getComponent();
        addRenderableWidget(iLabel);

        dLabel = new Label(0, 0, ExposedFieldType.D.getComponent()).colored(common_label_color);
        dLabel.text = ExposedFieldType.D.getComponent();
        addRenderableWidget(dLabel);

        tLabel = new Label(0, 0, ExposedFieldType.TARGET.getComponent()).colored(common_label_color);
        tLabel.text = ExposedFieldType.TARGET.getComponent();

        addRenderableWidget(tLabel);

        vLabel = new Label(0, 0, ExposedFieldType.VALUE.getComponent()).colored(common_label_color);
        vLabel.text = ExposedFieldType.VALUE.getComponent();
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

        ItemStack renderedItem = renderedItem();
        if(renderedItem == null)return;

        GuiGameElement.of(renderedItem).scale(3)
                .at(x, y, 0)
                .render(graphics);


    }

    protected ItemStack renderedItem(){
        return null;
    }

}
