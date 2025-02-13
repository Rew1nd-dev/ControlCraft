package com.verr1.vscontrolcraft.registry;

import com.simibubi.create.foundation.utility.Components;
import com.verr1.vscontrolcraft.ControlCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class AllCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ControlCraft.MODID);

    public static final RegistryObject<CreativeModeTab> TAB = REGISTER.register("controlcraft",
            () -> CreativeModeTab.builder()
                    .title(Components.translatable("itemGroup."+ ControlCraft.MODID +".main"))
                    .withTabsBefore(ResourceLocation.of("create:palettes", ':'))
                    .icon(AllBlocks.SPATIAL_ANCHOR_BLOCK::asStack)
                    .displayItems((params, output) -> {
                        List<ItemStack> items = ControlCraft.REGISTRATE.getAll(Registries.ITEM)
                                .stream()
                                .map((regItem) -> new ItemStack(regItem.get()))
                                .toList();
                        //output.accept(Items.STICK.getDefaultInstance());
                        //output.accept(AllBlocks.CHUNK_LOADER.asItem());
                        output.acceptAll(items);
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        ControlCraft.LOGGER.info("Registering Creative Tabs");
        REGISTER.register(modEventBus);
    }
}
