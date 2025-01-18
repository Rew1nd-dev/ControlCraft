package com.verr1.vscontrolcraft.blocks.propeller;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.verr1.vscontrolcraft.blocks.recevier.ReceiverRegisterPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class PropellerScreen extends AbstractSimiScreen {
    private final Component confirmLabel = Lang.translateDirect("action.trySettingPropeller");

    private AllGuiTextures background;
    private EditBox thrustRatioField;
    private EditBox torqueRatioField;
    private Checkbox reverseTorqueField;
    private IconButton register;

    private boolean reverseTorque;
    private double thrust_ratio;
    private double torque_ratio;
    private BlockPos pos;

    public PropellerScreen(BlockPos entityPos, boolean reverseTorque, double thrust_ratio, double torque_ratio) {
        background = AllGuiTextures.SCHEMATIC_PROMPT;
        pos = entityPos;
        this.reverseTorque = reverseTorque;
        this.thrust_ratio = thrust_ratio;
        this.torque_ratio = torque_ratio;
    }

    @Override
    public void init(){
        setWindowSize(background.width, background.height);
        super.init();

        int x = guiLeft;
        int y = guiTop;

        thrustRatioField = new EditBox(font, x + 49, y + 26, 131, 10, Components.immutableEmpty());
        thrustRatioField.setTextColor(-1);
        thrustRatioField.setTextColorUneditable(-1);
        thrustRatioField.setBordered(false);
        thrustRatioField.setMaxLength(35);
        thrustRatioField.setFocused(true);

        thrustRatioField.setValue(thrust_ratio + "");
        thrustRatioField.setFilter(Util::tryParseDoubleFilter);

        setFocused(thrustRatioField);
        addRenderableWidget(thrustRatioField);

        torqueRatioField = new EditBox(font, x + 49, y + 14, 131, 10, Components.immutableEmpty());
        torqueRatioField.setTextColor(-1);
        torqueRatioField.setTextColorUneditable(-1);
        torqueRatioField.setBordered(false);
        torqueRatioField.setMaxLength(35);
        torqueRatioField.setValue(torque_ratio + "");
        torqueRatioField.setFilter(Util::tryParseDoubleFilter);

        addRenderableWidget(torqueRatioField);

        reverseTorqueField = new Checkbox(x + 49, y + 38, 10, 10, Components.immutableEmpty(), reverseTorque);

        addRenderableWidget(reverseTorqueField);

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
                        new PropellerSettingsPacket(
                                pos,
                                reverseTorqueField.selected(),
                                Util.tryParseDouble(thrustRatioField.getValue()),
                                Util.tryParseDouble(torqueRatioField.getValue())
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

