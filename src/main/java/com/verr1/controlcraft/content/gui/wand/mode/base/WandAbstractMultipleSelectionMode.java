package com.verr1.controlcraft.content.gui.wand.mode.base;

import com.verr1.controlcraft.foundation.api.IWandMode;
import com.verr1.controlcraft.foundation.data.WandSelection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public abstract class WandAbstractMultipleSelectionMode implements IWandMode {
    private final int lazyTickRate = 50;
    private int lazyTick = 0;

    public enum State{
        TO_SELECT_X,
        TO_SELECT_Y,
        TO_SELECT_Z,
        TO_SELECT_W,
        TO_SELECT_A,
        TO_SELECT_B,
        TO_SELECT_C,
        TO_CONFIRM
    }

    protected WandSelection x = WandSelection.NULL;
    protected WandSelection y = WandSelection.NULL;
    protected WandSelection z = WandSelection.NULL;
    protected WandSelection w = WandSelection.NULL;
    protected WandSelection a = WandSelection.NULL;
    protected WandSelection b = WandSelection.NULL;
    protected WandSelection c = WandSelection.NULL;


    protected State state = State.TO_SELECT_X;
    protected State next_state = State.TO_SELECT_X;

    @Override
    public abstract IWandMode getInstance();

    @Override
    public abstract String getID();

    @Override
    public void onTick() {
        if(lazyTick-- < 0){
            lazyTick = lazyTickRate;
            lazyTick();
        }
        tick();
    }

    @Override
    public boolean isRunning() {
        return state != State.TO_SELECT_X;
    }

    @Override
    public void onClear() {
        clear();
        next_state = State.TO_SELECT_X;
        state = next_state;
    }

    protected void clear(){
        x = WandSelection.NULL;
        y = WandSelection.NULL;
        z = WandSelection.NULL;
        w = WandSelection.NULL;
        a = WandSelection.NULL;
        b = WandSelection.NULL;
        c = WandSelection.NULL;
    }

    protected boolean isValid(WandSelection selection){
        return (selection != null && selection.face() != null && selection.pos() != null);
    }

    protected void tick(){

    }

    protected void lazyTick(){

    }

}
