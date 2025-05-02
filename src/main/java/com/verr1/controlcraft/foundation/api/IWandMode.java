package com.verr1.controlcraft.foundation.api;

import com.simibubi.create.foundation.gui.AllIcons;
import com.verr1.controlcraft.foundation.data.WandSelection;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IWandMode {

    IWandMode getInstance();

    String getID();

    boolean isRunning();

    default void onSelection(WandSelection selection){}

    default void onConfirm(){}

    default void onClear(){}

    default void onDeselect(){}

    default void onTick(){}

    default String tickCallBackInfo(){return "";}

    default List<Component> getDescription(){
        return List.of();
    }

    default AllIcons getIcon(){
        return new AllIcons(0, 0);
    }

}
