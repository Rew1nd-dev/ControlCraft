package com.verr1.vscontrolcraft.blocks.anchor;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class AnchorScreen extends AbstractSimiScreen {
    private final Component confirmLabel = Lang.translateDirect("action.trySettingPropeller");

    private AllGuiTextures background;
    private EditBox aField;
    private EditBox eField;
    private IconButton register;

    private final double airResistance;
    private final double extraGravity;
    private final BlockPos pos;

    public AnchorScreen(BlockPos entityPos, double airResistance, double extraGravity) {
        background = AllGuiTextures.SCHEMATIC_PROMPT;
        pos = entityPos;
        this.airResistance = airResistance;
        this.extraGravity = extraGravity;
    }

    @Override
    public void init(){
        setWindowSize(background.width, background.height);
        super.init();

        int x = guiLeft;
        int y = guiTop;

        aField = new EditBox(font, x + 26, y + 2, 131, 10, Components.immutableEmpty());
        aField.setTextColor(-1);
        aField.setTextColorUneditable(-1);
        aField.setBordered(false);
        aField.setMaxLength(35);
        aField.setValue(airResistance + "");
        aField.setEditable(true);
        aField.setFilter(Util::tryParseDoubleFilter);

        addRenderableWidget(aField);

        eField = new EditBox(font, x + 26, y + 16, 131, 10, Components.immutableEmpty());
        eField.setTextColor(-1);
        eField.setTextColorUneditable(-1);
        eField.setBordered(false);
        eField.setMaxLength(35);
        eField.setValue(extraGravity + "");
        eField.setEditable(true);
        eField.setFilter(Util::tryParseDoubleFilter);

        addRenderableWidget(eField);


        register = new IconButton(x + 158, y + 53, AllIcons.I_CONFIRM);
        register.withCallback(() -> {
            register();
            onClose();
        });
        register.setToolTip(confirmLabel);
        addRenderableWidget(register);


    }


    @Override
    public void tick(){
        super.tick();
    }

    public void register() {
        AllPackets
                .getChannel()
                .sendToServer(
                        new AnchorSettingsPacket(
                                Util.tryParseDouble(aField.getValue()),
                                Util.tryParseDouble(eField.getValue()),
                                pos
                        )
                );
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        graphics.drawCenteredString(font, title, x + (background.width - 8) / 2, y + 3, 0xFFFFFF);

        GuiGameElement.of(AllItems.SCHEMATIC.asStack())
                .at(x + 22, y + 23, 0)
                .render(graphics);

        GuiGameElement.of(AllItems.SCHEMATIC_AND_QUILL.asStack())
                .scale(3)
                .at(x + background.width + 6, y + background.height - 40, -200)
                .render(graphics);
    }
}
