package com.verr1.controlcraft.content.gui.v1.widgets;

import com.simibubi.create.foundation.gui.element.ScreenElement;
import com.verr1.controlcraft.content.gui.v1.layouts.api.ComponentLike;
import com.verr1.controlcraft.content.gui.v1.layouts.api.Descriptive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class DescriptiveScrollInput<T extends Enum<?> & Descriptive<?>> extends IconSelectionScrollInput{

    private final ArrayList<T> values = new ArrayList<>();
    private Consumer<T> valueCalling = it -> {};


    public DescriptiveScrollInput(int xIn, int yIn, int widthIn, int heightIn, ScreenElement icon, Class<T> clazz) {
        super(xIn, yIn, widthIn, heightIn, icon);
        this.values.addAll(Arrays.asList(clazz.getEnumConstants()));
        lateInit();
    }

    public DescriptiveScrollInput(int xIn, int yIn, int widthIn, int heightIn, ScreenElement icon, @NotNull T[] provided){
        super(xIn, yIn, widthIn, heightIn, icon);
        this.values.addAll(Arrays.asList(provided));
        lateInit();
    }

    public DescriptiveScrollInput(int xIn, int yIn, int widthIn, int heightIn, ScreenElement icon){
        super(xIn, yIn, widthIn, heightIn, icon);
        // this.values = Arrays.asList(provided);

    }

    public DescriptiveScrollInput<T> withValues(T[] provided){
        values.clear();
        values.addAll(Arrays.asList(provided));
        lateInit();
        return this;
    }

    private void lateInit(){
        withDescriptions(
                Optional.of(values).filter(l -> !l.isEmpty()).map(aliases -> aliases.get(0).overall()).orElse(List.of())
        ).withOptionDescriptions(
                i -> Optional.of(i)
                        .filter(j -> j >= 0 && j < values.size())
                        .map(j -> values.get(j).specific())
                        .orElseGet(List::of)
        ).forOptions(
                values.stream()
                        .map(ComponentLike::asComponent)
                        .toList()
        ).withRange(0, values.size());
    }


    public List<T> values() {
        return values;
    }

    public @Nullable T valueOfOption(){
        return Optional.of(getState())
                .filter(it -> it >= 0 && it < values.size())
                .map(values::get)
                .orElse(null);
    }

    public DescriptiveScrollInput<T> valueCalling(Consumer<T> valueCalling){
        this.valueCalling = valueCalling;
        return this;
    }

    @Override
    public void onChanged() {
        super.onChanged();
        valueCalling.accept(valueOfOption());
    }

    public void setToValue(T value){
        try{
            setState(values.indexOf(value));
            onChanged();
        }catch (Exception ignored){

        }
    }


}
