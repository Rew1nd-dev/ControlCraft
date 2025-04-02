package com.verr1.controlcraft.utils;

import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.registry.ControlCraftDataGen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class LangUtils {

    private final static String NAME_SUFFIX = ".name";
    private final static String CLASS_SUFFIX = ".overall";
    private final static String LINE_SUFFIX = ".lines";

    private static String addIndex(String key, int index){
        return key + "." + index;
    }

    public static<T extends Enum<?>> int descriptionLinesOf(Class<T> clazz, T value){
        return (int)ParseUtils.tryParseLong(
                    Component.translatable(keyOf(clazz, value) + LINE_SUFFIX
                ).getString());
    }

    public static<T extends Enum<?>> String keyOf(Class<T> clazz, T value){
        return ControlCraft.MODID + "." + clazz.getSimpleName().toLowerCase() + "." + value.name().toLowerCase();
    }

    public static<T extends Enum<?>> String keyOf(Class<T> clazz){
        return ControlCraft.MODID + "." + clazz.getSimpleName().toLowerCase() + CLASS_SUFFIX;
    }

    public static<T extends Enum<?>> int descriptionLinesOf(Class<T> clazz){
        return (int)ParseUtils.tryParseLong(
                Component.translatable(keyOf(clazz) + LINE_SUFFIX).getString());
    }

    public static<T extends Enum<?>> Component nameOf(Class<T> clazz, T value){
        String keys = keyOf(clazz, value) + NAME_SUFFIX;
        return Component.translatable(keys);
    }

    public static<T extends Enum<?>> List<Component> descriptionsOf(Class<T> clazz, T value){
        String keys = keyOf(clazz, value);
        int totalLines = descriptionLinesOf(clazz, value);
        ArrayList<Component> list = new ArrayList<>();
        for (int i = 0; i < totalLines; i++) {
            String lineKey = addIndex(keys, i);
            list.add(Component.translatable(lineKey));
        }
        return list;
    }

    public static<T extends Enum<?>> List<Component> descriptionsOf(Class<T> clazz){
        String keys = keyOf(clazz);
        int totalLines = descriptionLinesOf(clazz);
        ArrayList<Component> list = new ArrayList<>();
        for (int i = 0; i < totalLines; i++) {
            String lineKey = addIndex(keys, i);
            list.add(Component.translatable(lineKey));
        }
        return list;
    }


    public static<T extends Enum<?>> void registerDefaultDescription(Class<T> clazz, T value, List<Component> descriptions){
        String mainKey = keyOf(clazz, value);
        int totalLines = descriptions.size();
        ControlCraftDataGen.registerExtraDescriptions(mainKey + LINE_SUFFIX, "" + descriptions.size());
        for (int i = 0; i < totalLines; i++) {
            String lineKey = addIndex(mainKey, i);
            ControlCraftDataGen.registerExtraDescriptions(lineKey, descriptions.get(i).getString());
        }
    }

    public static<T extends Enum<?>> void registerDefaultName(Class<T> clazz, T value , Component name){
        String mainKey = keyOf(clazz, value) + NAME_SUFFIX;
        ControlCraftDataGen.registerExtraDescriptions(mainKey, name.getString());
    }

    public static<T extends Enum<?>> void registerDefaultDescription(Class<T> clazz, List<Component> descriptions){
        String mainKey = keyOf(clazz);
        int totalLines = descriptions.size();
        ControlCraftDataGen.registerExtraDescriptions(mainKey + LINE_SUFFIX, "" + descriptions.size());
        for (int i = 0; i < totalLines; i++) {
            String lineKey = addIndex(mainKey, i);
            ControlCraftDataGen.registerExtraDescriptions(lineKey, descriptions.get(i).getString());
        }
    }

}
