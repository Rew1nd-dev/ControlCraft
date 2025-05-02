package com.verr1.controlcraft.content.gui.screens;


import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import com.verr1.controlcraft.content.blocks.terminal.ChannelWrapper;
import com.verr1.controlcraft.content.blocks.terminal.TerminalBlockEntity;
import com.verr1.controlcraft.registry.ControlCraftMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class TerminalMenu extends GhostItemMenu<ChannelWrapper> {

    public TerminalMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public TerminalMenu(MenuType<?> type, int id, Inventory inv, ChannelWrapper packet) {
        super(type, id, inv, packet);
    }

    public static TerminalMenu create(int id, Inventory inv, ChannelWrapper packet) {
        return new TerminalMenu(ControlCraftMenuTypes.TERMINAL.get(), id, inv, packet);
    }

    @Override
    protected ChannelWrapper createOnClient(FriendlyByteBuf extraData) {
        return new ChannelWrapper(extraData);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return TerminalBlockEntity.getFrequencyItems(contentHolder);
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(40 + 8, 131 + 24);

        int x = 8;
        int y = 8;
        int slot = 0;

        for (int column = 0; column < 2; ++column){
            for (int row = 0; row < 6; row++) {
                addSlot(new SlotItemHandler(ghostInventory, slot++, x + column * 18, y + row * 18));
            }
        }



    }

    @Override
    protected void saveData(ChannelWrapper contentHolder) {
        contentHolder.serialize(ghostInventory.serializeNBT());
    }

    @Override
    protected boolean allowRepeats() {
        return true;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId == playerInventory.selected && clickTypeIn != ClickType.THROW)
            return;
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }
}
