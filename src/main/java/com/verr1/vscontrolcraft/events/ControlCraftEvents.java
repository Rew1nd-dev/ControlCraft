package com.verr1.vscontrolcraft.events;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.Debug;
import com.verr1.vscontrolcraft.DeferralExecutor;
import com.verr1.vscontrolcraft.blocks.chunkLoader.ChunkManager;
import com.verr1.vscontrolcraft.compat.cctweaked.alternates.DelegateOnServerTickStart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ControlCraftEvents {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        ChunkManager.tick(event);
        DeferralExecutor.tick();
        Debug.tick(event);
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        DelegateOnServerTickStart.setServer(event.getServer());
    }

    public static void onPhysicsTick(){
        // ControlCraft.LOGGER.info("Physics tick");
        DelegateOnServerTickStart.onServerPhysicsTickStart();
    }
}
