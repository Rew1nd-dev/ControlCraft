package com.verr1.vscontrolcraft.base.Wand.render;

import com.simibubi.create.foundation.gui.AllIcons;
import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.mode.*;
import com.verr1.vscontrolcraft.base.Wand.mode.base.WandAbstractMultipleSelectionMode;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public enum WandModesType {


    HINGE,
    SERVO,
    // LEVEL,
    // FLIP,
    JOINT,
    DESTROY,
    SLIDER;


    public static final HashMap<WandModesType, IWandMode> modeOf = new HashMap<>();

    public String langKey = "";

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
        modeOf.put(DESTROY, WandDestroyConstrainMode.instance);
        modeOf.put(SERVO, WandServoAssembleMode.instance);
        // modeOf.put(LEVEL, WandAdjustHingeLevelMode.instance);
        // modeOf.put(FLIP, WandFlipRevoluteJointMode.instance);
        modeOf.put(JOINT, WandJointAssembleMode.instance);
        modeOf.put(SLIDER, WandSliderAssembleMode.instance);
        //KeyPressEventBus.registerListener(ClientWand::OnWandRightClick);
    };

    private WandModesType(String key){
        this.langKey = key;
    }

    private WandModesType(){
        this.langKey = name().toLowerCase();
    }


    public static IWandMode modeOf(WandModesType type){
        return modeOf.get(type);
    }

    public List<Component> getDescription(){
        List<Component> descriptions = new ArrayList<>();
        int lines = (int)Util.tryParseLong(Component.translatable("wand.mode.description.lines." + langKey).getString());
        for (int i = 1; i <= lines; i++){
            descriptions.add(Component.translatable("wand.mode.description." + langKey + "." + i));
        }
        return descriptions;
    }

    public String getID(){
        return Component.translatable("wand.mode.name." + langKey).getString();
    }

    public AllIcons getIcon(){
        return new AllIcons(0, 0);
    }

    public static List<WandModesType> getAllTypes(){
        return Arrays.stream(WandModesType.values()).toList();
    }

    public Component tickCallBackInfo(WandAbstractMultipleSelectionMode.State state) {
        return Component.translatable("wand.mode.callback." + state.name().toLowerCase() + "." + langKey);
    }

}
