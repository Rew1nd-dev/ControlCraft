package com.verr1.vscontrolcraft.registry;
import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.verr1.vscontrolcraft.items.SpinalyzerLinkerItem;
import net.minecraft.resources.ResourceLocation;

import static com.verr1.vscontrolcraft.ControlCraft.REGISTRATE;

public class AllItems {
    static {
        REGISTRATE.setCreativeTab(AllCreativeTabs.TAB);
    }

    public static final ItemEntry<SpinalyzerLinkerItem> LINKER = REGISTRATE.item("linker", SpinalyzerLinkerItem::new)
            .model((c, p) -> p.withExistingParent(c.getName(), p.mcLoc("item/barrier")))
            .properties(p -> p.stacksTo(1))
            .lang("Example Item")
            .register();

    public static void register(){

    }
}
