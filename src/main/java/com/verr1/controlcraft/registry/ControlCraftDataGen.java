package com.verr1.controlcraft.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.foundation.type.descriptive.*;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ControlCraftDataGen {

    private static final HashMap<String, String> EXTRA_DESCRIPTIONS = new HashMap<>();


    public static void gatherData(GatherDataEvent event) {


        ControlCraft.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
            BiConsumer<String, String> langConsumer = provider::add;
            // provideDefaultLang(langConsumer);
            provideExtra(langConsumer);
        });

    }

    public static void registerExtraDescriptions(String key, String value){
        EXTRA_DESCRIPTIONS.put(key, value);
    }

    public static void provideExtra(BiConsumer<String, String> consumer){
        for (Map.Entry<String, String> entry : EXTRA_DESCRIPTIONS.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            consumer.accept(key, value);
        }
    }

    private static void provideDefaultLang(BiConsumer<String, String> consumer) {
        String path = "assets/vscontrolcraft/lang/default/" + "vscontrolcraft" + ".json";
        JsonElement jsonElement = FilesHelper.loadJsonResource(path);
        if (jsonElement == null) {
            throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();
            consumer.accept(key, value);
        }
    }

    public static void registerEnumDescriptions(){
        CheatMode.register();
        WandGUIModesType.register();
        LockMode.register();
        ExposedFieldType.register();
        ExposedFieldDirection.register();
        MiscDescription.register();
        UIContents.register();
        TargetMode.register();
    }

}
