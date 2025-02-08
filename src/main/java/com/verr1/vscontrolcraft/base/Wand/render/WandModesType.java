package com.verr1.vscontrolcraft.base.Wand.render;

import com.simibubi.create.foundation.gui.AllIcons;
import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.mode.*;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public enum WandModesType {


    HINGE,
    SERVO,
    LEVEL,
    FLIP,
    JOINT,
    DESTROY,
    SLIDER;


    public static final HashMap<WandModesType, IWandMode> modeOf = new HashMap<>();

    static {
        WandHingeConnectionMode.createInstance();
        WandServoAssembleMode.createInstance();
        WandAdjustHingeLevelMode.createInstance();
        WandFlipRevoluteJointMode.createInstance();
        WandJointAssembleMode.createInstance();
        WandDestroyConstrainMode.createInstance();
        WandSliderAssembleMode.createInstance();
        WandAnchorConnect.createInstance();
        modeOf.put(HINGE, WandHingeConnectionMode.instance);
        modeOf.put(SERVO, WandServoAssembleMode.instance);
        modeOf.put(LEVEL, WandAdjustHingeLevelMode.instance);
        modeOf.put(FLIP, WandFlipRevoluteJointMode.instance);
        modeOf.put(JOINT, WandJointAssembleMode.instance);
        modeOf.put(DESTROY, WandDestroyConstrainMode.instance);
        modeOf.put(SLIDER, WandSliderAssembleMode.instance);
        //KeyPressEventBus.registerListener(ClientWand::OnWandRightClick);
    };


    public static IWandMode modeOf(WandModesType type){
        return modeOf.get(type);
    }

    public List<Component> getDescription(){
        return List.of();
    }

    public String getID(){
        return name();
    }

    public AllIcons getIcon(){
        return new AllIcons(0, 0);
    }

    public static List<WandModesType> getAllTypes(){
        return Arrays.stream(WandModesType.values()).toList();
    }
}
