package com.verr1.controlcraft.foundation.type;

import com.simibubi.create.foundation.gui.AllIcons;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum WandGUIModesType {
    CONNECT,
    DISCONNECT,
    DISCONNECT_ALL;

    public List<Component> getDescription(){
        java.util.List<net.minecraft.network.chat.Component> descriptions = new ArrayList<>();
        int lines = (int) ParseUtils.tryParseLong(net.minecraft.network.chat.Component.translatable("wand.mode.description.lines." + langKey).getString());
        for (int i = 1; i <= lines; i++){
            descriptions.add(net.minecraft.network.chat.Component.translatable("wand.mode.description." + langKey + "." + i));
        }
        return descriptions;
    }

    public final String langKey;

    WandGUIModesType(){
        this.langKey = name().toLowerCase();
    }

    public String getID(){
        return Component.translatable("wand.mode.name." + langKey).getString();
    }

    public AllIcons getIcon(){
        return AllIcons.I_TOOLBOX;
    }

    public static List<WandGUIModesType> getAllTypes(){
        return Arrays.stream(WandGUIModesType.values()).toList();
    }
}
