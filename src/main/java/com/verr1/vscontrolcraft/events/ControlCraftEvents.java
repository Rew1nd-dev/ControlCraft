package com.verr1.vscontrolcraft.events;

import com.verr1.vscontrolcraft.Debug;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.base.IntervalExecutor.IntervalExecutor;
import com.verr1.vscontrolcraft.blocks.chunkLoader.ChunkManager;
import com.verr1.vscontrolcraft.compat.cctweaked.alternates.ComputerCraftDelegation;
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
        IntervalExecutor.tick();
        Debug.tick(event);
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {

    }

    public static void onPhysicsTickStart(){
        // ControlCraft.LOGGER.info("Physics tick");
        // DelegateOnServerTickStart.DelegateRun();
        ComputerCraftDelegation.FreeDelegateThread();
    }

    public static void onPhysicsTickEnd(){
        ComputerCraftDelegation.LockDelegateThread();
    }
}
