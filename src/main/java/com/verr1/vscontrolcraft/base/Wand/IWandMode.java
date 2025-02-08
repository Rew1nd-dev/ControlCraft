package com.verr1.vscontrolcraft.base.Wand;

import com.mojang.datafixers.TypeRewriteRule;
import com.simibubi.create.foundation.gui.AllIcons;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IWandMode {

    IWandMode getInstance();

    String getID();

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
