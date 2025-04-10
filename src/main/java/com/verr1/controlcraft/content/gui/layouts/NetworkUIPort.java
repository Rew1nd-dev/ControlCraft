package com.verr1.controlcraft.content.gui.layouts;

import com.verr1.controlcraft.content.gui.layouts.api.TabListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.GridLayout;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class NetworkUIPort<T> implements TabListener {
    private final Consumer<T> write;
    private final Supplier<T> read;
    private final GridLayout layout = new GridLayout();
    protected boolean isActivated = false;

    public NetworkUIPort(Consumer<T> write, Supplier<T> read){
        this.write = write;
        this.read = read;
    }

    public GridLayout layout(){
        return layout;
    }

    public void onScreenInit(){
        initLayout(layout);
    }

    public void readToLayout(){
        writeGUI(read.get());
    }

    public void onScreenTick(){}

    public void onActivatedTab(){
        isActivated = true;
        layout.visitWidgets(e -> e.visible = true);
    }

    @Override
    public void onMessage(Message msg) {

    }

    @Override
    public void onAddRenderable(Collection<AbstractWidget> toAdd) {
        layout.visitWidgets(toAdd::add);
    }

    public void onRemovedTab(){
        isActivated = false;
        layout.visitWidgets(e -> e.visible = false);
    }


    public final void writeFromLayout(){
        T value = readGUI();
        if(value == null)return;
        write.accept(value);
    }


    protected abstract void initLayout(GridLayout layoutToFill);
    protected abstract T readGUI();
    protected abstract void writeGUI(T value);


}
