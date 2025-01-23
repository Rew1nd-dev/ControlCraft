package com.verr1.vscontrolcraft.registry;
import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.verr1.vscontrolcraft.items.AweInWandItem;

import static com.verr1.vscontrolcraft.ControlCraft.REGISTRATE;

public class AllItems {
    static {
        REGISTRATE.setCreativeTab(AllCreativeTabs.TAB);
    }

    public static final ItemEntry<AweInWandItem> ALL_IN_WAND = REGISTRATE.item("awe_in_wand", AweInWandItem::new)
            .model(AssetLookup.existingItemModel())
            .properties(p -> p.stacksTo(1))
            .lang("Awe-In-Wand")
            .register();


    public static void register(){

    }
}
