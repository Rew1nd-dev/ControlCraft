package com.verr1.controlcraft.registry;

import com.simibubi.create.Create;
import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.verr1.controlcraft.content.gui.screens.TerminalScreen;
import com.verr1.controlcraft.foundation.redstone.TerminalMenu;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ControlCraftMenuTypes {


    public static final MenuEntry<TerminalMenu> TERMINAL =
            register("terminal_", TerminalMenu::new, () -> TerminalScreen::new);

    private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>> MenuEntry<C> register(
            String name, MenuBuilder.ForgeMenuFactory<C> factory, NonNullSupplier<MenuBuilder.ScreenFactory<C, S>> screenFactory) {
        return Create.REGISTRATE
                .menu(name, factory, screenFactory)
                .register();
    }


    public static void register() {}
}
