package com.verr1.vscontrolcraft.base.Wand;

import com.verr1.vscontrolcraft.base.Wand.render.WandModesType;
import com.verr1.vscontrolcraft.registry.AllItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(value = Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientWand{


    public static WandModesType currentType = WandModesType.DESTROY;

    public static void select(WandSelection selection){
        WandModesType.modeOf(currentType).onSelection(selection);
    }

    public static void deselect(){
        WandModesType.modeOf(currentType).onDeselect();
    }

    public static void flush(){
        WandModesType.modeOf(currentType).onClear();
    }

    public static void confirm(){
        WandModesType.modeOf(currentType).onConfirm();
    }

    public static void tick(){
        WandModesType.modeOf(currentType).onTick();
    }

    /*
    public static void switchUpMode(){
        index = ((index + 1) % modes.size());
        informLocalPlayer();
    }

    public static void switchDownMode(){
        index = ((index - 1 + modes.size()) % modes.size());
        informLocalPlayer();
    }
    * */


    public static void informLocalPlayer(){
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null)return;
        player.sendSystemMessage(Component.literal("current mode: " + WandModesType.modeOf(currentType).getID()));
    }

    public static void setMode(WandModesType mode){
        currentType = mode;
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event){
        if(!event.getLevel().isClientSide)return;
        if(event.getHand() != InteractionHand.MAIN_HAND)return;
        if(!isClientWandInHand())return;
        WandSelection selection = new WandSelection(event.getPos(), event.getFace());
        select(selection);
    }

    @SubscribeEvent
    public static void onWandClick(InputEvent.InteractionKeyMappingTriggered event){
        if(isClientWandInHand())event.setSwingHand(false);
    }


    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event){
        if(!event.getLevel().isClientSide)return;
        if(!isClientWandInHand())return;
        if(!event.getEntity().isShiftKeyDown())confirm();
        if( event.getEntity().isShiftKeyDown())flush();
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event){

        if(!event.getLevel().isClientSide)return;
        if(!isClientWandInHand())return;
        flush();
        /*
        if(!event.getEntity().isShiftKeyDown())switchUpMode();
        if( event.getEntity().isShiftKeyDown())switchDownMode();
        * */


    }

    public static boolean isClientWandInHand(){
        if(Minecraft.getInstance().player == null)return false;
        return Minecraft.getInstance().player.getMainHandItem().getItem() == AllItems.ALL_IN_WAND.get();
    }

    // ||
    //

    public static boolean isWrenchInHand(){
        if(Minecraft.getInstance().player == null)return false;
        return Minecraft.getInstance().player.getMainHandItem().is(com.simibubi.create.AllItems.WRENCH.asItem());
    }

}
