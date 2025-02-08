package com.verr1.vscontrolcraft.registry;

import com.verr1.vscontrolcraft.ControlCraft;
import net.minecraft.data.DataProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DataGen {
    @SubscribeEvent
    public void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(
                // Tell generator to run only when client assets are generating
                event.includeClient(),
                // Localizations for American English
                (DataProvider.Factory<AllLang>) output -> new AllLang(output, ControlCraft.MODID, "en_us")
        );
    }
}
