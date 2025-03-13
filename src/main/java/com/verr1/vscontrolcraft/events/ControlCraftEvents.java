package com.verr1.vscontrolcraft.events;

import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.ControlCraftClient;
import com.verr1.vscontrolcraft.Debug;
import com.verr1.vscontrolcraft.base.Constrain.ConstrainCenter;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.base.IntervalExecutor.IntervalExecutor;
import com.verr1.vscontrolcraft.blocks.chunkLoader.ChunkManager;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetManager;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialLinkManager;
import com.verr1.vscontrolcraft.blocks.transmitter.NetworkManager;
import com.verr1.vscontrolcraft.compat.cctweaked.alternates.ComputerCraftDelegation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
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
        MagnetManager.tick();
        NetworkManager.tick();
        ConstrainCenter.tick();
        ComputerCraftDelegation.tick();
        SpatialLinkManager.tick();
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        ConstrainCenter.onServerStaring(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ConstrainCenter.onServerStopping(event.getServer());
    }

    @SubscribeEvent
    public static void onFireChunkWatch(ChunkWatchEvent.Watch event){
        if(event.getPlayer() instanceof FakePlayer)return;
        ChunkPos p = event.getChunk().getPos();
        ControlCraft.LOGGER.info("player fire chunk watch: " + p.x + " " + p.z);
    }




    public static void onPhysicsTickStart(){
        ComputerCraftDelegation.FreeDelegateThread();
    }

    public static void onPhysicsTickEnd(){
        ComputerCraftDelegation.LockDelegateThread();
    }
}
