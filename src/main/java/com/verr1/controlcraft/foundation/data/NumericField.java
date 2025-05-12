package com.verr1.controlcraft.foundation.data;

import com.verr1.controlcraft.foundation.api.Field;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NumericField implements Field<Double> {
    public static final NumericField EMPTY = new NumericField(() -> 0.0, (v) -> {}, "empty");

    private final Supplier<Double> value;
    private final Consumer<Double> callback;
    private final String name;


    public NumericField(Supplier<Double> value, Consumer<Double> callback, String name) {
        this.value = value;
        this.callback = callback;
        this.name = name;
    }


    @Override
    public Double value(){
        return value.get();
    };

    @Override
    public void apply(Double value){
        callback.accept(value);
    }

    @Override
    public String name() {
        return name;
    }



}
