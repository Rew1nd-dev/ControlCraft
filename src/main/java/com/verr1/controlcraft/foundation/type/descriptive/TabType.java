package com.verr1.controlcraft.foundation.type.descriptive;

import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.utils.ComponentUtils;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.verr1.controlcraft.utils.ComponentUtils.literals;
import static net.minecraft.network.chat.Component.literal;

public enum TabType implements Descriptive<TabType> {
    GENERIC(
            literals("Common Settings"),
            literal("Generic")
    ),

    REDSTONE(
            literals("Expose Property To Redstone Input"),
            literal("Redstone")
    ),

    CONTROLLER(
            literals(
                    "Integrated Controller Settings",
                    "Don't Touch It If You Don't Know What It Is"
                ),
            literal("Controller")
    ),

    REMOTE(
            literals(
                    "Remote Device Panel",
                    "Ask Device To Do Something"
                ),
            literal("Remote")
    ),


    ;

    TabType(List<Component> descriptions, Component name){
        LangUtils.registerDefaultName(TabType.class, this, name);
        LangUtils.registerDefaultDescription(TabType.class, this, descriptions);
    }




    @Override
    public TabType self() {
        return this;
    }

    @Override
    public Class<TabType> clazz() {
        return TabType.class;
    }

    public static void register(){
        // load by class loader and constructors will call registerDefaultName etc
    }
}
