package com.verr1.controlcraft.foundation.redstone;

import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import com.verr1.controlcraft.content.blocks.terminal.TerminalBlockEntity;
import com.verr1.controlcraft.content.blocks.terminal.WrappedChannel;
import com.verr1.controlcraft.content.gui.container.TerminalItemSlot;
import com.verr1.controlcraft.registry.ControlCraftMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.ItemStackHandler;

import static com.verr1.controlcraft.content.gui.screens.TerminalScreen.LINE_BLOCK;

public class TerminalMenu extends GhostItemMenu<WrappedChannel> {

    public TerminalMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    public TerminalMenu(MenuType<?> type, int id, Inventory inv, WrappedChannel packet) {
        super(type, id, inv, packet);
    }

    public static TerminalMenu create(int id, Inventory inv, WrappedChannel packet) {
        return new TerminalMenu(ControlCraftMenuTypes.TERMINAL.get(), id, inv, packet);
    }

    @Override
    protected WrappedChannel createOnClient(FriendlyByteBuf extraData) {
        return new WrappedChannel(extraData);
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return TerminalBlockEntity.getFrequencyItems(contentHolder);
    }


    public void setPage(int page){
        slots.stream().filter(TerminalItemSlot.class::isInstance).map(TerminalItemSlot.class::cast).forEach(
                s -> s.setActive(s.page == page)
        );
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(40 + 8, 131 + 24);

        int totalPages = (contentHolder.size() - 1) / LINE_BLOCK + 1;

        int x = 8;
        int y = 8;
        int slot = 0;

        for(int p = 0; p < totalPages; p++){
            for (int row = 0; row < LINE_BLOCK; ++row) {
                for (int column = 0; column < 2; ++column){
                    if(slot >= ghostInventory.getSlots())break;
                    addSlot(new TerminalItemSlot(ghostInventory, slot++, x + column * 18, y + row * 18)
                            .withPage(p)
                            .active(p == 0)
                    );
                }
            }

        }





    }

    @Override
    protected void saveData(WrappedChannel contentHolder) {
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
