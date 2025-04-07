package com.verr1.controlcraft.utils;

import com.verr1.controlcraft.ControlCraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ComponentUtils {

    public static List<Component> literals(String... components){
        ArrayList<Component> list = new ArrayList<>();
        for (String component : components) {
            list.add(Component.literal(component));
        }
        return list;
    }


    public static List<Component> translatables(String... keys){
        ArrayList<Component> list = new ArrayList<>();
        for (String key : keys) {
            list.add(Component.translatable(key));
        }
        return list;
    }



}
