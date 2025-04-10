package com.verr1.controlcraft.foundation.type.descriptive;

import com.simibubi.create.foundation.gui.AllIcons;
import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

import static com.verr1.controlcraft.utils.ComponentUtils.literals;

public enum WandGUIModesType implements Descriptive<WandGUIModesType> {
    CONNECT(literals(
            "Connect Ships To Device",
            "Currently Applicable:", "| Motors | Piston | Joints |")),
    DISCONNECT(literals(
            "Disconnect Ships From Device",
            "Currently Applicable:", "| Motors | Piston | Joints |")),
    DISCONNECT_ALL(literals(
            "Click Ship To Apply, Destroy All Constraints",
            "Won't Reset Device, Need Manually Replace",
            "Don't Use This Unless There's Some ",
            "Constraint Cannot Be Removed Normally"
            )),;

    @Override
    public WandGUIModesType self() {
        return this;
    }

    @Override
    public Class<WandGUIModesType> clazz() {
        return WandGUIModesType.class;
    }

    WandGUIModesType(List<Component> description){
        LangUtils.registerDefaultName(WandGUIModesType.class, this, Component.literal(name()));
        LangUtils.registerDefaultDescription(WandGUIModesType.class, this, description);
    }


    public AllIcons getIcon(){
        return AllIcons.I_TOOLBOX;
    }

    public static List<WandGUIModesType> getAllTypes(){
        return Arrays.stream(WandGUIModesType.values()).toList();
    }
    public static void register(){
        // load by class loader and constructors will call registerDefaultName etc
    }

}
