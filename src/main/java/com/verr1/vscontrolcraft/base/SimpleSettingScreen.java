package com.verr1.vscontrolcraft.base;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllGuiTextures;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class SimpleSettingScreen extends AbstractSimiScreen {

    private final AllGuiTextures background = AllGuiTextures.SIMPLE_BACKGROUND_QUARTER;

    private IconButton register;

    protected final List<EditBox> iFields = new ArrayList<>(); // inputs
    protected final List<EditBox> tFields = new ArrayList<>(); // tags
    protected int labelWidth = 50;
    protected int lineHeight = 12;
    protected int fieldWidth = 40;

    protected int labelColor = new Color(250, 250, 180).getRGB();

    public EditBox addFieldWithLabel(String label, Predicate<String> filter){
        var newI = new EditBox(font, 0, 0, fieldWidth, lineHeight, Component.literal(""));
        var newT = new EditBox(font, 0, 0, labelWidth, lineHeight, Component.literal(""));

        newI.setBordered(true);
        newI.setMaxLength(fieldWidth);
        newI.setFilter(filter);
        newI.setEditable(true);

        newT.setBordered(false);
        newT.setMaxLength(labelWidth);
        newT.setEditable(false);
        newT.setValue(label);
        newT.setTextColorUneditable(labelColor);

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
        tFields.clear();

        startWindow();
        register = new IconButton(0, 0, AllIcons.I_CONFIRM);
        register.withCallback(this::register);
        addRenderableWidget(register);

        GridLayout statLayout = new GridLayout(background.width, background.height);
        statLayout.setX(guiLeft + 4);
        statLayout.setY(guiTop  + 4);




        for(int i = 0; i < iFields.size(); i++){
            statLayout.addChild(tFields.get(i), i, 0);
            statLayout.addChild(iFields.get(i), i, 1);
        }


        // statLayout.addChild(register, iFields.size(), 0);

        register.setToolTip(Component.literal("set Params"));
        register.setY(guiTop + windowHeight - 12 - 22);
        register.setX(guiLeft + 4);
        statLayout.columnSpacing(4).rowSpacing(2);
        statLayout.arrangeElements();
    }

    public abstract void startWindow();

    public abstract void register();

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
