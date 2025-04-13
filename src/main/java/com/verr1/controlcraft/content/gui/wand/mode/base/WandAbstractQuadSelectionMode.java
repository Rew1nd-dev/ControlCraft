package com.verr1.controlcraft.content.gui.wand.mode.base;


import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.foundation.data.WandSelection;
import com.verr1.controlcraft.foundation.managers.ClientOutliner;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(value = Dist.CLIENT)
public abstract class WandAbstractQuadSelectionMode extends WandAbstractMultipleSelectionMode {

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


    @Override
    public void tick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if(x != WandSelection.NULL) ClientOutliner.drawOutline(x.pos(), x.face(), 0xaaca32, "x");
        if(y != WandSelection.NULL) ClientOutliner.drawOutline(y.pos(), y.face(), 0xffcb74, "y");
        if(z != WandSelection.NULL) ClientOutliner.drawOutline(z.pos(), z.face(), 0xaabbcc, "z");
        if(w != WandSelection.NULL) ClientOutliner.drawOutline(w.pos(), w.face(), 0xff66cc, "w");

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
