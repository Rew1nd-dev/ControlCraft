package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public abstract class WandAbstractQuadSelectionMode implements IWandMode {
    protected WandSelection x = WandSelection.NULL;
    protected WandSelection y = WandSelection.NULL;
    protected WandSelection z = WandSelection.NULL;
    protected WandSelection w = WandSelection.NULL;

    protected enum State{
        TO_SELECT_X,
        TO_SELECT_Y,
        TO_SELECT_Z,
        TO_SELECT_W,
        TO_CONFIRM
    }

    protected State state = State.TO_SELECT_X;
    protected State next_state = State.TO_SELECT_X;


    protected WandAbstractQuadSelectionMode(){

    }

    @Override
    public void onSelection(WandSelection selection) {
        switch (state){
            case TO_SELECT_X:
                x = selection;
                next_state = State.TO_SELECT_Y;
                break;
            case TO_SELECT_Y:
                y = selection;
                next_state = State.TO_SELECT_Z;
                break;
            case TO_SELECT_Z:
                z = selection;
                next_state = State.TO_SELECT_W;
                break;
            case TO_SELECT_W:
                w = selection;
                next_state = State.TO_CONFIRM;
                break;
        }
        state = next_state;
    }

    @Override
    public void onConfirm() {
        switch (state){
            case TO_SELECT_X, TO_SELECT_Y, TO_SELECT_Z, TO_SELECT_W:
                break;
            case TO_CONFIRM:
                confirm();
                clear();
                next_state = State.TO_SELECT_X;
                break;
        }
        state = next_state;
    }

    private void clear(){
        x = WandSelection.NULL;
        y = WandSelection.NULL;
        z = WandSelection.NULL;
        w = WandSelection.NULL;
    }

    @Override
    public void onTick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if(x != WandSelection.NULL) WandRenderer.drawOutline(x.pos(), x.face(), 0xaaca32, "x");
        if(y != WandSelection.NULL) WandRenderer.drawOutline(y.pos(), y.face(), 0xffcb74, "y");
        if(z != WandSelection.NULL) WandRenderer.drawOutline(z.pos(), z.face(), 0xaabbcc, "z");
        if(w != WandSelection.NULL) WandRenderer.drawOutline(w.pos(), w.face(), 0xff66cc, "w");

    }

    private void confirm(){
        if(x == WandSelection.NULL || y == WandSelection.NULL || z == WandSelection.NULL || w == WandSelection.NULL){
            ControlCraft.LOGGER.info("Invalid state");
            return;
        }

        sendPacket(x, y, z, w);
    }

    protected abstract void sendPacket(WandSelection x, WandSelection y, WandSelection z, WandSelection w);

    @Override
    public abstract String getID();
}
