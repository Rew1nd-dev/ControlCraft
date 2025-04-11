package com.verr1.controlcraft.foundation.type.descriptive;

import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.verr1.controlcraft.utils.ComponentUtils.literals;

public enum MiscDescription implements Descriptive<MiscDescription> {

    KINEMATIC_TOOLTIP_MOTTO(literals(
            "Does Not Respect Physics",
            "But At What Cost?"
    )),

    KINEMATIC_TOOLTIP_CAUTION(
            literals(
                    "DO NOT DESTROY A Ship",
                    "Which is Connected By This Device",
                    "Otherwise Physics Thread May Crash"
            )
    ),


    CAUTION(literals(
            "CAUTION: "
    )),

    DUMP(literals(
            "Dump Settings"
    )),

    AS_REDSTONE_INPUT(literals(
            "Receive Direct Redstone Input"
    )),

    REVERSE_INPUT(literals(
            "Integrated Negate"
    )),

    TURN_ON(literals(
            "Enable This Channel"
    )),

    ;


    MiscDescription(List<Component> descriptions){
        LangUtils.registerDefaultDescription(MiscDescription.class, this, descriptions);
    }

    @Override
    public MiscDescription self() {
        return this;
    }

    @Override
    public Class<MiscDescription> clazz() {
        return MiscDescription.class;
    }

    public static void register(){}

}
