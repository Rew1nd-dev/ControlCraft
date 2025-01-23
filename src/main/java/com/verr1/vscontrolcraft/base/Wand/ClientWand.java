package com.verr1.vscontrolcraft.base.Wand;

import com.verr1.vscontrolcraft.base.Wand.mode.WandAdjustHingeLevelMode;
import com.verr1.vscontrolcraft.base.Wand.mode.WandHingeConnectionMode;
import com.verr1.vscontrolcraft.base.Wand.mode.WandServoAssembleMode;
import com.verr1.vscontrolcraft.registry.AllItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;


@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientWand {

    static {
        WandHingeConnectionMode.createInstance();
        WandServoAssembleMode.createInstance();
        WandAdjustHingeLevelMode.createInstance();

    }
    static List<IWandMode> modes = List.of(
            WandHingeConnectionMode.instance,
            WandServoAssembleMode.instance,
            WandAdjustHingeLevelMode.instance
    );

    private static int index = 0;

    public static void select(WandSelection selection){
        modes.get(index).onSelection(selection);
    }

    public static void deselect(){
        modes.get(index).onDeselect();
    }

    public static void flush(){
        modes.get(index).onClear();
    }

    public static void confirm(){
        modes.get(index).onConfirm();
    }

    public static void tick(){
        modes.get(index).onTick();
    }

    public static void switchUpMode(){
        index = ((index + 1) % modes.size());
        informLocalPlayer();
    }

    public static void switchDownMode(){
        index = ((index - 1) % modes.size());
        informLocalPlayer();

    }

    public static void informLocalPlayer(){
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null)return;
        player.sendSystemMessage(Component.literal("current mode: " + modes.get(index).getID()));
    }


    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
        if(!event.getLevel().isClientSide)return;
        if(event.getItemStack().getItem() != AllItems.ALL_IN_WAND.get())return;
        WandSelection selection = new WandSelection(event.getPos(), event.getFace());
        select(selection);
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event){
        if(!event.getLevel().isClientSide)return;
        if(!event.getEntity().isShiftKeyDown())confirm();
        if( event.getEntity().isShiftKeyDown())flush();
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event){
        if(!event.getLevel().isClientSide)return;
        if(event.getItemStack().getItem() != AllItems.ALL_IN_WAND.get())return;
        flush();
        if(!event.getEntity().isShiftKeyDown())switchUpMode();
        if( event.getEntity().isShiftKeyDown())switchDownMode();

    }

}
