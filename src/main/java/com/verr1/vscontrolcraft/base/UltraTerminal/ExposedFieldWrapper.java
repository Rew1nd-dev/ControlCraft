package com.verr1.vscontrolcraft.base.UltraTerminal;

import org.joml.Vector2d;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ExposedFieldWrapper {

    public static ExposedFieldWrapper EMPTY = new ExposedFieldWrapper(NumericField.EMPTY, ExposedFieldType.NONE);

    public NumericField field;
    public Vector2d min_max;
    public ExposedFieldType type;


    public ExposedFieldWrapper(Supplier<Double> value, Consumer<Double> callback, String name, WidgetType widgetType, ExposedFieldType type){
        this.field = new NumericField(value, callback, name, widgetType);
        this.min_max = new Vector2d(0, 1);
        this.type = type;
    }


    public ExposedFieldWrapper(NumericField field, ExposedFieldType type){
        this.field = field;
        this.min_max = new Vector2d(0, 1);
        this.type = type;
    }


    public void apply(int signal){
        field.apply(min_max.x + (min_max.y - min_max.x) * signal / 15);
    }

}
