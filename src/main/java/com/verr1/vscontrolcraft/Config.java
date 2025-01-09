package com.verr1.vscontrolcraft;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = ControlCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue CC_OverLocking = BUILDER
            .comment(
                    "------------------------------------",
                    "  Warning: It May Break Your Saves!!!",
                    "  Warning: It May Break Your Saves!!!",
                    "  Warning: It May Break Your Saves!!!",
                    "------------------------------------",
                    "  By Default, ComputerCraft Is Running at Game Thread. ",
                    "  When Enable This Settings, ComputerCraft Will Run at Another Thread Which Is Synced By VS Physics Thread. ",
                    "  This Feature Is Currently **Experimental**, It Might Cause Unknown Concurrent Issues")
            .define("Enable Physics Thread Synced ComputerCraft", false);


    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean OverclockComputerCraft;


    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        OverclockComputerCraft = CC_OverLocking.get();
        /* convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());
        * */


    }
}
