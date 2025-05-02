package com.verr1.controlcraft.events;

import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.content.blocks.transmitter.NetworkManager;
import com.verr1.controlcraft.content.cctweaked.delegation.ComputerCraftDelegation;
import com.verr1.controlcraft.content.commands.ControlCraftCommands;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.managers.ChunkManager;
import com.verr1.controlcraft.foundation.managers.ConstraintCenter;
import com.verr1.controlcraft.foundation.managers.SpatialLinkManager;
import com.verr1.controlcraft.foundation.type.descriptive.MiscDescription;
import com.verr1.controlcraft.registry.ControlCraftAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.valkyrienskies.core.api.ships.LoadedServerShip;

@Mod.EventBusSubscriber
public class ControlCraftEvents {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // AttachmentRegistry.register();
        BlockEntityGetter.create(event.getServer());
        ConstraintCenter.onServerStaring(event.getServer());
        ControlCraftServer.INSTANCE = event.getServer();
        ControlCraftAttachments.register();

        // VSEvents.ShipLoadEvent.Companion.on(ControlCraftAttachments::onShipLoad);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // ControlCraftServer.SERVER_INTERVAL_EXECUTOR.tick();
        ControlCraftServer.SERVER_EXECUTOR.tick();
        SpatialLinkManager.tick();
        ChunkManager.tick(event);
        NetworkManager.tick();
    }


    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event){
        if(!ModList.get().isLoaded("patchouli")){
            event.getEntity().sendSystemMessage(
                    Component.literal("[Control Craft]").withStyle(s -> s.withColor(ChatFormatting.GOLD).withBold(true))
                            .append(
                                    MiscDescription.SUGGEST_PATCHOULI.specific().stream().reduce(
                                            Component.empty(),
                                            (a, b) -> a.copy().append(Component.literal(" ")).append(b)
                                    )
            ));
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ConstraintCenter.onServerStopping(event.getServer());
    }

    public static void onPhysicsTickStart(){
        ComputerCraftDelegation.LockDelegateThread();
    }

    public static void onPhysicsTickEnd(){
        ComputerCraftDelegation.FreeDelegateThread();
    }

}
