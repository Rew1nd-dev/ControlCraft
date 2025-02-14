package com.verr1.vscontrolcraft.base;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldRequestPacket;
import com.verr1.vscontrolcraft.blocks.terminal.SmallCheckbox;
import com.verr1.vscontrolcraft.registry.AllGuiLabels;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.registry.AllVSCCGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class SimpleSettingScreen extends AbstractSimiScreen {

    protected final AllVSCCGuiTextures background = AllVSCCGuiTextures.SIMPLE_BACKGROUND_QUARTER;

    protected IconButton register;
    protected IconButton redstoneSettings;

    // TODO: Wrap these two types, otherwise it's very easy to crash things
    protected final List<SmallCheckbox> bFields = new ArrayList<>();
    protected final List<EditBox> iFields = new ArrayList<>(); // inputs
    protected final List<Label> tFields = new ArrayList<>(); // tags
    protected int labelWidth = 50;
    protected int lineHeight = 12;
    protected int fieldWidth = 40;

    protected int labelColor = new Color(250, 250, 180).getRGB();


    public SmallCheckbox addBooleanFieldWithLabel(Component label){
        var newI = new SmallCheckbox(0, 0, fieldWidth, lineHeight, Component.literal(""), false);
        var newT = new Label(0, 0, label).colored(labelColor);

        newT.text = label;


        addRenderableWidget(newI);
        addRenderableWidget(newT);

        bFields.add(newI);
        tFields.add(newT);

        return newI;
    }

    public EditBox addNumericFieldWithLabel(Component label, Predicate<String> filter){
        var newI = new EditBox(font, 0, 0, fieldWidth, lineHeight, Component.literal(""));
        var newT = new Label(0, 0, label).colored(labelColor);

        newI.setBordered(true);
        newI.setMaxLength(fieldWidth);
        newI.setFilter(filter);
        newI.setEditable(true);

        newT.text = label;

        addRenderableWidget(newI);
        addRenderableWidget(newT);

        iFields.add(newI);
        tFields.add(newT);

        return newI;
    }

    @Override
    public void init(){
        setWindowSize(background.width, background.height);
        super.init();

        iFields.clear();
        bFields.clear();
        tFields.clear();

        startWindow();
        register = new IconButton(0, 0, AllIcons.I_CONFIRM);
        register.withCallback(this::register);
        addRenderableWidget(register);

        redstoneSettings = new IconButton(0, 0, AllIcons.I_ACTIVE);
        redstoneSettings.withCallback(this::redstoneSetting);
        addRenderableWidget(redstoneSettings);

        GridLayout statLayout = new GridLayout(background.width, background.height);
        statLayout.setX(guiLeft + 4);
        statLayout.setY(guiTop  + 4);




        for(int i = 0; i < iFields.size(); i++){
            statLayout.addChild(tFields.get(i), i, 0);
            statLayout.addChild(iFields.get(i), i, 1);
        }

        for(int i = iFields.size(); i < iFields.size() + bFields.size(); i++){
            int j = i - iFields.size();
            statLayout.addChild(tFields.get(i), i, 0);
            statLayout.addChild(bFields.get(j), i, 1);
        }


        // statLayout.addChild(register, iFields.size(), 0);

        register.setToolTip(AllGuiLabels.confirmLabel);
        register.setY(guiTop + windowHeight - 12 - 22);
        register.setX(guiLeft + 4);

        redstoneSettings.setToolTip(AllGuiLabels.redstoneLabel);
        redstoneSettings.setY(guiTop + windowHeight - 12 - 22);
        redstoneSettings.setX(guiLeft + 4 + 20);

        statLayout.columnSpacing(4).rowSpacing(2);
        statLayout.arrangeElements();
    }

    public abstract void startWindow();

    public abstract void register();

    public abstract @NotNull BlockPos getPos();

    public void redstoneSetting(){
        AllPackets.getChannel().sendToServer(
                new ExposedFieldRequestPacket(
                        getPos()
                )
        );
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        background.render(graphics, guiLeft, guiTop);
        ItemStack renderStack = renderedStack();
        int x = guiLeft + 30;
        int y = guiTop + background.height;

        if(renderStack == null)return;
        GuiGameElement.of(renderStack)
                .scale(3)
                .at(x, y, 0)
                .render(graphics);
    }

    protected abstract @Nullable ItemStack renderedStack();

}
