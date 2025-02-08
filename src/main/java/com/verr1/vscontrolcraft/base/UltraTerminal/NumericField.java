package com.verr1.vscontrolcraft.base.UltraTerminal;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NumericField implements Field<Double>{
    public static NumericField EMPTY = new NumericField(() -> 0.0, (v) -> {}, "empty", WidgetType.SLIDE);

    private Supplier<Double> value;
    private Consumer<Double> callback;
    private String name;
    private WidgetType widgetType;

    public NumericField(Supplier<Double> value, Consumer<Double> callback, String name, WidgetType widgetType) {
        this.value = value;
        this.callback = callback;
        this.name = name;
        this.widgetType = widgetType;
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

    @Override
    public WidgetType widgetType() {
        return widgetType;
    }


}
