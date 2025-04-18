package com.verr1.controlcraft.events;


import com.verr1.controlcraft.ControlCraftClient;
import com.verr1.controlcraft.foundation.managers.ClientCameraManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ControlCraftClientEvents {

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event){
        ControlCraftClient.CLIENT_EXECUTOR.tick();
        // ControlCraftClient.CLIENT_INTERVAL_EXECUTOR.tick();
        ControlCraftClient.CLIENT_WAND_HANDLER.tick();
        ClientCameraManager.tick();
    }


    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null)
            return;
        int key = event.getKey();
        boolean pressed = !(event.getAction() == 0);

        ControlCraftClient.CLIENT_WAND_HANDLER.onKeyInput(key, pressed);
    }

    @SubscribeEvent
    public static void onMouseScrolled(InputEvent.MouseScrollingEvent event) {
        ControlCraftClient.CLIENT_WAND_HANDLER.onMouseScroll(event);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event){
        ControlCraftClient.CLIENT_WAND_HANDLER.onRightClick(event);
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event){
        ControlCraftClient.CLIENT_WAND_HANDLER.onWandClick(event);
    }


    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event){
        ControlCraftClient.CLIENT_WAND_HANDLER.onRightClickEmpty(event.getEntity().isShiftKeyDown());
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event){
        ControlCraftClient.CLIENT_WAND_HANDLER.onLeftClickEmpty(event);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModeBusEvents{
        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "wand", ControlCraftClient.CLIENT_WAND_HANDLER);

        }


    }



}
