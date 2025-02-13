package com.verr1.vscontrolcraft.base.UltraTerminal;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.joml.Vector2d;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ExposedFieldWrapper {

    public static ExposedFieldWrapper EMPTY = new ExposedFieldWrapper(NumericField.EMPTY, ExposedFieldType.NONE);

    public NumericField field;
    public Vector2d min_max;
    public ExposedFieldType type;
    public ExposedFieldDirection directionOptional = ExposedFieldDirection.NONE;


    public ExposedFieldWrapper(Supplier<Double> value, Consumer<Double> callback, String name, WidgetType widgetType, ExposedFieldType type){
        this.field = new NumericField(value, callback, name, widgetType);
        this.min_max = new Vector2d(0, 1);
        this.type = type;
    }

    public void withMcDirection(Direction direction){
        directionOptional = ExposedFieldDirection.convert(direction);
    }

    public void withDirection(ExposedFieldDirection direction){
        directionOptional = direction;
    }

    public ExposedFieldWrapper(NumericField field, ExposedFieldType type){
        this.field = field;
        this.min_max = new Vector2d(0, 1);
        this.type = type;
    }

    public ExposedFieldWrapper withSuggestedRange(double min, double max){
        min_max = new Vector2d(min, max);
        return this;
    }

    public CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        tag.putDouble("min", min_max.x);
        tag.putDouble("max", min_max.y);
        tag.putString("type", type.name());
        return tag;
    }

    public void deserialize(CompoundTag tag){
        min_max = new Vector2d(tag.getDouble("min"), tag.getDouble("max"));
        type = ExposedFieldType.valueOf(tag.getString("type"));
    }


    public void apply(int signal){
        field.apply(min_max.x + (min_max.y - min_max.x) * signal / 15);
    }

}
