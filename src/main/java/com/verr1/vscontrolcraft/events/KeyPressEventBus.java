package com.verr1.vscontrolcraft.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class KeyPressEventBus {
    public static class MouseButtonEvent extends PlayerEvent {
        final long windowPointer;
        final int button;
        final int action;
        final int modifiers;

        boolean shouldCancel = false;

        public MouseButtonEvent(long windowPointer, int button, int action, int modifiers){
            super(Minecraft.getInstance().player);
            this.windowPointer = windowPointer;
            this.button = button;
            this.action = action;
            this.modifiers = modifiers;
        }

        @Override
        public void setCanceled(boolean cancel){
            shouldCancel = cancel;
        }

        public boolean getCanceled(){
            return shouldCancel;
        }

        public boolean isRightPressed(){
            return button == 1 && action == 1;
        }

        public HitResult getHitResult(){
            return Minecraft.getInstance().hitResult;
        }

        public ItemStack getItemStack(InteractionHand hand){
            return getEntity().getItemInHand(hand);
        }

    }

    public record EventCallBack(boolean shouldCancel){}


    private static final List<Consumer<MouseButtonEvent>> EventListeners = new LinkedList<>();

    public static void registerListener(Consumer<MouseButtonEvent> listener){
        EventListeners.add(listener);
    }

    public static EventCallBack fireEvent(MouseButtonEvent event){
        if(!isInGame())return new EventCallBack(false);
        EventListeners.forEach(listener -> listener.accept(event));
        return new EventCallBack(event.getCanceled());
    }

    public static boolean isInGame(){
        return Minecraft.getInstance().player != null;
    }
}
