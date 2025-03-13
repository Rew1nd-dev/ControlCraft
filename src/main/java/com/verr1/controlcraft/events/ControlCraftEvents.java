package com.verr1.controlcraft.events;

import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.content.blocks.transmitter.NetworkManager;
import com.verr1.controlcraft.content.valkyrienskies.attachments.*;
import com.verr1.controlcraft.foundation.ServerBlockEntityGetter;
import com.verr1.controlcraft.foundation.managers.ConstraintCenter;
import com.verr1.controlcraft.foundation.managers.SpatialLinkManager;
import com.verr1.controlcraft.registry.ControlCraftAttachments;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.valkyrienskies.core.impl.hooks.VSEvents;
import org.valkyrienskies.mod.api.ValkyrienSkies;

@Mod.EventBusSubscriber
public class ControlCraftEvents {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // AttachmentRegistry.register();
        ServerBlockEntityGetter.create(event.getServer());
        ConstraintCenter.onServerStaring(event.getServer());
        ControlCraftServer.INSTANCE = event.getServer();
        ControlCraftAttachments.register();

        VSEvents.ShipLoadEvent.Companion.on(ControlCraftAttachments::onShipLoad);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        ControlCraftServer.SERVER_INTERVAL_EXECUTOR.tick();
        ControlCraftServer.SERVER_DEFERRAL_EXECUTOR.tick();
        SpatialLinkManager.tick();
        NetworkManager.tick();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ConstraintCenter.onServerStopping(event.getServer());
    }


}
