package com.verr1.vscontrolcraft.base.Wand.render;

import com.simibubi.create.AllKeys;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.elevator.ElevatorControlsHandler;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.schematics.client.SchematicHotbarSlotOverlay;
import com.simibubi.create.content.schematics.client.SchematicRenderer;
import com.simibubi.create.content.schematics.client.ToolSelectionScreen;
import com.simibubi.create.content.trains.TrainHUD;
import com.verr1.vscontrolcraft.ControlCraftClient;
import com.verr1.vscontrolcraft.base.Wand.ClientWand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.naming.ldap.Control;
import java.util.Vector;

public class WandGUI implements IGuiOverlay {

    private SchematicHotbarSlotOverlay overlay;
    private ModeSelectionScreen selectionScreen;
    private int activeHotbarSlot = 0;


    public WandGUI(){
        overlay = new SchematicHotbarSlotOverlay();
        selectionScreen = new ModeSelectionScreen(WandModesType.getAllTypes(), ClientWand::setMode);
    }



    public boolean onMouseScroll(double delta) {

        if (selectionScreen.focused) {
            selectionScreen.cycle((int) delta);
            return true;
        }
        return false;
    }

    public void onKeyInput(int key, boolean pressed) {
        if (!ClientWand.isClientWandInHand())
            return;
        if (key != AllKeys.TOOL_MENU.getBoundCode())
            return;

        if (pressed && !selectionScreen.focused)
            selectionScreen.focused = true;
        if (!pressed && selectionScreen.focused) {
            selectionScreen.focused = false;
            selectionScreen.onClose();
        }
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height) {
        if (Minecraft.getInstance().options.hideGui)
            return;
        if (ClientWand.isClientWandInHand()){
            this.overlay.renderOn(graphics, activeHotbarSlot);
            selectionScreen.renderPassive(graphics, partialTicks);
        }

    }

    public void tick(){
        if(!ClientWand.isClientWandInHand())return;
        selectionScreen.update();
    }

}
