package com.verr1.controlcraft.content.gui.v1.layouts.api;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.Collection;

public interface TabListener{

    void onActivatedTab();

    void onRemovedTab();

    void onScreenTick();

    void onAddRenderable(Collection<AbstractWidget> toAdd);

}
