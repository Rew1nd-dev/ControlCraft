package com.verr1.vscontrolcraft.blocks.recevier;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class ReceiverScreen extends AbstractSimiScreen {

    private final Component confirmLabel = Lang.translateDirect("action.trySettingNetworkID");

    private AllGuiTextures background;
    private EditBox nameField;
    private EditBox peripheralType;
    private EditBox peripheralProtocol;
    private IconButton register;

    private String receivedName;
    private String receivedType;
    private long receivedProtocol;
    private BlockPos pos;

    public ReceiverScreen(BlockPos entityPos, String name, String peripheralType, long protocol) {
        background = AllGuiTextures.SCHEMATIC_PROMPT;
        pos = entityPos;
        receivedName = name;
        receivedType = peripheralType;
        receivedProtocol = protocol;
    }

    @Override
    public void init(){
        setWindowSize(background.width, background.height);
        super.init();

        int x = guiLeft;
        int y = guiTop;

        nameField = new EditBox(font, x + 49, y + 26, 131, 10, Components.immutableEmpty());
        nameField.setTextColor(-1);
        nameField.setTextColorUneditable(-1);
        nameField.setBordered(false);
        nameField.setMaxLength(35);
        nameField.setFocused(true);

        nameField.setValue(receivedName);

        setFocused(nameField);
        addRenderableWidget(nameField);

        peripheralType = new EditBox(font, x + 49, y + 14, 131, 10, Components.immutableEmpty());
        peripheralType.setTextColor(-1);
        peripheralType.setTextColorUneditable(-1);
        peripheralType.setBordered(false);
        peripheralType.setMaxLength(35);
        peripheralType.setEditable(false);
        peripheralType.setValue(receivedType);

        addRenderableWidget(peripheralType);

        peripheralProtocol = new EditBox(font, x + 49, y + 38, 131, 10, Components.immutableEmpty());
        peripheralProtocol.setTextColor(-1);
        peripheralProtocol.setTextColorUneditable(-1);
        peripheralProtocol.setBordered(false);
        peripheralProtocol.setMaxLength(35);
        peripheralProtocol.setValue(receivedProtocol + "");
        peripheralProtocol.setFilter(s -> {
            if(s.isEmpty()) return true;
            try{
                Long.parseLong(s);
                return true;
            }catch (NumberFormatException e){
                return false;
            }
        });

        addRenderableWidget(peripheralProtocol);

        register = new IconButton(x + 158, y + 53, AllIcons.I_CONFIRM);
        register.withCallback(() -> {
            register();
            onClose();
        });
        register.setToolTip(confirmLabel);
        addRenderableWidget(register);


    }

    public long tryParseProtocol(){
        try{
            return Long.parseLong(peripheralProtocol.getValue());
        }catch (NumberFormatException e){
            return -1;
        }
    }

    @Override
    public void tick(){
        super.tick();
    }

    public void register() {

        AllPackets
                .getChannel()
                .sendToServer(
                        new ReceiverRegisterPacket(
                                pos,
                                nameField.getValue(),
                                tryParseProtocol()
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
