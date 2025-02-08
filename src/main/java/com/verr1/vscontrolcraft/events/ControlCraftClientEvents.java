package com.verr1.vscontrolcraft.events;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.equipment.armor.RemainingAirOverlay;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.content.equipment.toolbox.ToolboxHandlerClient;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerClientHandler;
import com.simibubi.create.content.trains.TrainHUD;
import com.simibubi.create.content.trains.track.TrackPlacementOverlay;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.ControlCraftClient;
import com.verr1.vscontrolcraft.base.ICameraAccessor;
import com.verr1.vscontrolcraft.base.Wand.ClientWand;
import com.verr1.vscontrolcraft.blocks.camera.CameraBlockEntity;
import com.verr1.vscontrolcraft.blocks.camera.LinkedCameraManager;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerTargetHandler;
import com.verr1.vscontrolcraft.registry.AllLang;
import net.minecraft.client.Minecraft;
import net.minecraft.data.DataProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static dan200.computercraft.api.ComputerCraftAPI.MOD_ID;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ControlCraftClientEvents {

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event){
        LinkedCameraManager.tick();
        ClientWand.tick();

        ControlCraftClient.ClientWandHandler.tick();
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event){
        float partialTicks = (float)event.getPartialTick();
        CameraBlockEntity camera = LinkedCameraManager.getLinkedCamera();
        if(camera == null)return;
        if(event.getCamera() instanceof ICameraAccessor mixinedCamera){
            mixinedCamera.controlCraft$setDetached(true);
        }
    }

    @SubscribeEvent
    public static void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
        if (Minecraft.getInstance().screen != null)
            return;

        double delta = event.getScrollDelta();

        boolean cancelled = ControlCraftClient.ClientWandHandler.onMouseScroll(delta);
        event.setCanceled(cancelled);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null)
            return;
        int key = event.getKey();
        boolean pressed = !(event.getAction() == 0);

        ControlCraftClient.ClientWandHandler.onKeyInput(key, pressed);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModeBusEvents{
        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            // Register overlays in reverse order
            event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "wand", ControlCraftClient.ClientWandHandler);

        }
    }



}
