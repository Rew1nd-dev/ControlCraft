package com.verr1.controlcraft.content.gui.layouts;

import net.minecraft.client.gui.components.AbstractButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ButtonGroup<T extends AbstractButton> {

    private final List<T> buttons = new ArrayList<>();

    public ButtonGroup<T> reset(){
        buttons.clear();
        return this;
    }

    public ButtonGroup<T> withButton(T button){
        buttons.add(button);
        return this;
    }

    public ButtonGroup<T> withButtons(T[] buttons){
        this.buttons.addAll(Arrays.asList(buttons));
        return this;
    }

    public void visitExcept(Predicate<T> filter, Consumer<T> callback){
        buttons.stream()
                .filter(filter)
                .forEach(callback);
    }

}
