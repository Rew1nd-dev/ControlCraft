package com.verr1.controlcraft.content.gui.legacy;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.widgets.SmallCheckbox;
import com.verr1.controlcraft.foundation.network.packets.GenericServerPacket;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftGuiLabels;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class SimpleSettingScreen extends AbstractSimiScreen {

    protected final ControlCraftGuiTextures background = ControlCraftGuiTextures.SIMPLE_BACKGROUND_QUARTER;
    private final BlockPos pos;
    protected IconButton register;
    protected IconButton redstoneSettings;
    // protected SpruceButtonWidget testButton;

    // TODO: Wrap these two types, otherwise it's very easy to crash things
    protected final List<SmallCheckbox> bFields = new ArrayList<>();
    protected final List<EditBox> iFields = new ArrayList<>(); // inputs
    protected final List<Label> tFields = new ArrayList<>(); // tags
    protected int labelWidth = 50;
    protected int lineHeight = 12;
    protected int fieldWidth = 40;

    protected int labelColor = new Color(250, 250, 180).getRGB();

    public SimpleSettingScreen(BlockPos pos) {
        this.pos = pos;
    }




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


    public BlockPos getPos() {
        return pos;
    }

    public EditBox addNumericFieldWithLabel(Component label, Predicate<String> filter, boolean editable){
        var newI = new EditBox(font, 0, 0, fieldWidth, lineHeight, Component.literal(""));
        var newT = new Label(0, 0, label).colored(labelColor);

        newI.setBordered(true);
        newI.setMaxLength(fieldWidth);
        newI.setFilter(filter);
        newI.setEditable(editable);

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

        register.setToolTip(ControlCraftGuiLabels.confirmLabel);
        register.setY(guiTop + windowHeight - 12 - 22);
        register.setX(guiLeft + 4);

        redstoneSettings.setToolTip(ControlCraftGuiLabels.redstoneLabel);
        redstoneSettings.setY(guiTop + windowHeight - 12 - 22);
        redstoneSettings.setX(guiLeft + 4 + 20);

        statLayout.columnSpacing(4).rowSpacing(3);
        statLayout.arrangeElements();
    }

    public abstract void startWindow();

    public abstract void register();

    public void redstoneSetting(){
        var p = new GenericServerPacket.builder(RegisteredPacketType.GENERIC_REQUEST_EXPOSED_FIELDS)
                .withLong(getPos().asLong())
                .build();

        ControlCraftPackets.getChannel().sendToServer(p);
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
