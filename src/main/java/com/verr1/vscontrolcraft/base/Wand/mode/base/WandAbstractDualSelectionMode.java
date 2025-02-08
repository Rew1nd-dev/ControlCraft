package com.verr1.vscontrolcraft.base.Wand.mode.base;

import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(value = Dist.CLIENT)
public abstract class WandAbstractDualSelectionMode extends WandAbstractMultipleSelectionMode {

    protected WandAbstractDualSelectionMode(){

    }

    @Override
    public abstract String getID();

    @Override
    public void onSelection(WandSelection selection) {
        switch (state){
            case TO_SELECT_X:
                x = selection;
                next_state = State.TO_SELECT_Y;
                break;
            case TO_SELECT_Y:
                y = selection;
                next_state = State.TO_CONFIRM;
                break;
            case TO_CONFIRM:
                next_state = State.TO_CONFIRM;
                break;
        }
        state = next_state;
    }


    @Override
    public void onConfirm() {
        switch (state){
            case TO_SELECT_X, TO_SELECT_Y:
                // clear();
                // next_state = State.TO_SELECT_X;
                break;
            case TO_CONFIRM:
                confirm();
                clear();
                next_state = State.TO_SELECT_X;
                break;
        }
        state = next_state;
    }



    @Override
    public void tick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if(x != WandSelection.NULL) WandRenderer.drawOutline(x.pos(), x.face(), 0xaaca32, "source");
        if(y != WandSelection.NULL) WandRenderer.drawOutline(y.pos(), y.face(), 0xffcb74, "target");
    }

    private void confirm(){
        if(x == WandSelection.NULL || y == WandSelection.NULL){
            ControlCraft.LOGGER.info("Invalid state");
            return;
        }

        sendPacket(x, y);
    }

    protected abstract void sendPacket(WandSelection x, WandSelection y);
}
