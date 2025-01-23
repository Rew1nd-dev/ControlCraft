package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.Servo.ServoMotorConstrainAssemblePacket;
import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public abstract class WandAbstractDualSelectionMode implements IWandMode {

    private WandSelection x = WandSelection.NULL;
    private WandSelection y = WandSelection.NULL;

    private enum State{
        TO_SELECT_X,
        TO_SELECT_Y,
        TO_CONFIRM
    }

    private State state = State.TO_SELECT_X;
    private State next_state = State.TO_SELECT_X;


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
    public void onClear() {
        clear();
        next_state = State.TO_SELECT_X;
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



    private void clear(){
        x = WandSelection.NULL;
        y = WandSelection.NULL;
    }

    @Override
    public void onTick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if(x != WandSelection.NULL) WandRenderer.drawOutline(x.pos(), 0xaaca32, "source");
        if(y != WandSelection.NULL) WandRenderer.drawOutline(y.pos(), 0xffcb74, "target");
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
