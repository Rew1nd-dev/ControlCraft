package com.verr1.controlcraft.content.gui.container;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class TerminalItemSlot extends SlotItemHandler {

    protected boolean active = true;
    public int page = 0;

    public TerminalItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    public TerminalItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, boolean isActive) {
        super(itemHandler, index, xPosition, yPosition);
        setActive(isActive);
    }

    public TerminalItemSlot withPage(int page){
        this.page = page;
        return this;
    }

    public TerminalItemSlot active(boolean active){
        this.active = active;
        return this;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
