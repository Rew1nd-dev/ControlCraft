package com.verr1.vscontrolcraft;

import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.verr1.vscontrolcraft.registry.AllPartialModels;
import com.verr1.vscontrolcraft.render.CachedBufferer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ControlCraftClient {
    public static final SuperByteBufferCache BUFFER_CACHE = new SuperByteBufferCache();

    public static void clientInit(){
        ControlCraft.LOGGER.info("Try CC Generic Block");
        BUFFER_CACHE.registerCompartment(CachedBufferer.CC_GENERIC_BLOCK);
        ControlCraft.LOGGER.info("Try CC Partial");
        BUFFER_CACHE.registerCompartment(CachedBufferer.CC_PARTIAL);
        ControlCraft.LOGGER.info("Try CC Directional Partial");
        BUFFER_CACHE.registerCompartment(CachedBufferer.CC_DIRECTIONAL_PARTIAL);
        AllPartialModels.init();
    }
}
