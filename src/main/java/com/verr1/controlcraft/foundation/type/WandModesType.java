package com.verr1.controlcraft.foundation.type;

import com.simibubi.create.foundation.gui.AllIcons;
import com.verr1.controlcraft.content.wand.mode.*;
import com.verr1.controlcraft.content.wand.mode.base.WandAbstractMultipleSelectionMode;
import com.verr1.controlcraft.foundation.api.IWandMode;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public enum WandModesType {


    HINGE,
    SERVO,
    JOINT,
    DESTROY,
    DESTROY_ALL,
    SLIDER;


    public static final HashMap<WandModesType, IWandMode> modeOf = new HashMap<>();

    public String langKey = "";

    static {
        WandServoAssembleMode.createInstance();
        WandJointAssembleMode.createInstance();
        WandSliderAssembleMode.createInstance();
        WandJointConnectionMode.createInstance();
        WandDestroyConstrainMode.createInstance();
        WandDestroyAllConstrainMode.createInstance();
        modeOf.put(HINGE, WandJointConnectionMode.instance);
        modeOf.put(DESTROY, WandDestroyConstrainMode.instance);
        modeOf.put(DESTROY_ALL, WandDestroyAllConstrainMode.instance);
        modeOf.put(SERVO, WandServoAssembleMode.instance);
        modeOf.put(JOINT, WandJointAssembleMode.instance);
        modeOf.put(SLIDER, WandSliderAssembleMode.instance);
    };


    private WandModesType(){
        this.langKey = name().toLowerCase();
    }


    public static IWandMode modeOf(WandModesType type){
        return modeOf.get(type);
    }



    public Component tickCallBackInfo(WandAbstractMultipleSelectionMode.State state) {
        return Component.translatable("wand.mode.callback." + state.name().toLowerCase() + "." + langKey);
    }

}
