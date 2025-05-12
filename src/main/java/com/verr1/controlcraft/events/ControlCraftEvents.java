package com.verr1.controlcraft.events;

import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.content.blocks.transmitter.NetworkManager;
import com.verr1.controlcraft.content.cctweaked.delegation.ComputerCraftDelegation;
import com.verr1.controlcraft.content.compact.tweak.TweakedLinkedControllerServerHandlerExtension;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.managers.ChunkManager;
import com.verr1.controlcraft.foundation.managers.ConstraintCenter;
import com.verr1.controlcraft.foundation.managers.SpatialLinkManager;
import com.verr1.controlcraft.foundation.type.descriptive.MiscDescription;
import com.verr1.controlcraft.registry.ControlCraftAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

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
    public static void onServerWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START && event.side != LogicalSide.CLIENT) {
            Level world = event.level;
            TweakedLinkedControllerServerHandlerExtension.tick(world);
        }
    }


    @SubscribeEvent
    public static void onLoadWorld(LevelEvent.Load event) {
        LevelAccessor world = event.getLevel();
        ControlCraftServer.DECIMAL_LINK_NETWORK_HANDLER.onLoadWorld(world);
    }

    @SubscribeEvent
    public static void onUnloadWorld(LevelEvent.Unload event) {
        LevelAccessor world = event.getLevel();
        ControlCraftServer.DECIMAL_LINK_NETWORK_HANDLER.onUnloadWorld(world);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event){
        if(!ModList.get().isLoaded("patchouli")){
            event.getEntity().sendSystemMessage(
                    Component.literal("[Control Craft]")
                            .withStyle(s -> s.withColor(ChatFormatting.GOLD).withBold(true).withUnderlined(true))
                            .append(
                                    MiscDescription.SUGGEST_PATCHOULI.specific().stream().reduce(
                                            Component.empty(),
                                            (a, b) -> a.copy().append(Component.literal(" ")).append(b)
                                                    .withStyle(s -> s.withBold(false).withColor(ChatFormatting.AQUA))
                                    )
            ));
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ConstraintCenter.onServerStopping(event.getServer());
    }

    public static void onPhysicsTickStart(){
        ComputerCraftDelegation.lockDelegateThread();
    }

    public static void onPhysicsTickEnd(){
        ComputerCraftDelegation.freeDelegateThread();
    }

}
