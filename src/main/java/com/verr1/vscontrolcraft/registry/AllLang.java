package com.verr1.vscontrolcraft.registry;

import com.verr1.vscontrolcraft.ControlCraft;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import site.siredvin.peripheralium.data.language.ModInformationHolder;
import site.siredvin.peripheralium.data.language.TextRecord;

public class AllLang extends LanguageProvider {

    public AllLang(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {
        this.add("vscontrolcraft.controller.p", "Proportional Ratio");
        this.add("vscontrolcraft.controller.i", "Integral Ratio");
        this.add("vscontrolcraft.controller.d", "Deferential Ratio");
    }


}
