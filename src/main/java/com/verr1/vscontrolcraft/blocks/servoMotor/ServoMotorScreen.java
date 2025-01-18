package com.verr1.vscontrolcraft.blocks.servoMotor;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerSettingsPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class ServoMotorScreen extends AbstractSimiScreen {
    private final Component confirmLabel = Lang.translateDirect("action.trySettingPropeller");

    private AllGuiTextures background;
    private EditBox pField;
    private EditBox dField;
    private EditBox iField;
    private EditBox aField;
    private IconButton register;

    private double i;
    private double p;
    private double d;
    private double a;
    private BlockPos pos;

    public ServoMotorScreen(BlockPos entityPos, double p, double i, double d, double a) {
        background = AllGuiTextures.SCHEMATIC_PROMPT;
        pos = entityPos;
        this.p = p;
        this.i = i;
        this.d = d;
        this.a = a;
    }

    @Override
    public void init(){
        setWindowSize(background.width, background.height);
        super.init();

        int x = guiLeft;
        int y = guiTop;

        aField = new EditBox(font, x + 49, y + 2, 131, 10, Components.immutableEmpty());
        aField.setTextColor(-1);
        aField.setTextColorUneditable(-1);
        aField.setBordered(false);
        aField.setMaxLength(35);
        aField.setValue(a + "");
        aField.setFilter(s -> Util.tryParseClampedDoubleFilter(s, Math.PI));

        addRenderableWidget(aField);

        iField = new EditBox(font, x + 49, y + 14, 131, 10, Components.immutableEmpty());
        iField.setTextColor(-1);
        iField.setTextColorUneditable(-1);
        iField.setBordered(false);
        iField.setMaxLength(35);
        iField.setFocused(true);

        iField.setValue(i + "");
        iField.setFilter(Util::tryParseDoubleFilter);

        setFocused(iField);
        addRenderableWidget(iField);

        pField = new EditBox(font, x + 49, y + 26, 131, 10, Components.immutableEmpty());
        pField.setTextColor(-1);
        pField.setTextColorUneditable(-1);
        pField.setBordered(false);
        pField.setMaxLength(35);
        pField.setValue(p + "");
        pField.setFilter(Util::tryParseDoubleFilter);

        addRenderableWidget(pField);

        dField = new EditBox(font, x + 49, y + 38, 131, 10, Components.immutableEmpty());
        dField.setTextColor(-1);
        dField.setTextColorUneditable(-1);
        dField.setBordered(false);
        dField.setMaxLength(35);
        dField.setValue(d + "");
        dField.setFilter(Util::tryParseDoubleFilter);

        addRenderableWidget(dField);

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
                        new ServoMotorSettingsPacket(
                                Util.tryParseDouble(pField.getValue()),
                                Util.tryParseDouble(iField.getValue()),
                                Util.tryParseDouble(dField.getValue()),
                                Util.tryParseDouble(aField.getValue()),
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
