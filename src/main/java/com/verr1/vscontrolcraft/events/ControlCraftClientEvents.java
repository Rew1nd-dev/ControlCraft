package com.verr1.vscontrolcraft.events;

import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerTargetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ControlCraftClientEvents {

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event){
        SpinalyzerTargetHandler.tick();
    }

}
