package com.verr1.vscontrolcraft.base.Wand;

public interface IWandMode {

    IWandMode getInstance();

    String getID();

    default void onSelection(WandSelection selection){}

    default void onConfirm(){}

    default void onClear(){}

    default void onDeselect(){}

    default void onTick(){}
}
