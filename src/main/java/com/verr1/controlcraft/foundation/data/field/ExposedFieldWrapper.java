package com.verr1.controlcraft.foundation.data.field;

import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.foundation.data.NumericField;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldDirection;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ExposedFieldWrapper {

    public static ExposedFieldWrapper EMPTY = new ExposedFieldWrapper(NumericField.EMPTY, ExposedFieldType.NONE);

    public NumericField field;
    public Couple<Double> min_max = Couple.create(0.0, 1.0);
    public Couple<Double> suggestedMinMax = Couple.create(0.0, 1.0);
    public ExposedFieldType type;
    public ExposedFieldDirection directionOptional = ExposedFieldDirection.NONE;


    public ExposedFieldWrapper(Supplier<Double> value, Consumer<Double> callback, String name, ExposedFieldType type){
        this.field = new NumericField(value, callback, name);
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
        this.type = type;
    }

    public ExposedFieldWrapper withSuggestedRange(double min, double max){
        if(type.isBoolean())return this;
        min_max = Couple.create(min, max);
        suggestedMinMax = Couple.create(min, max);

        return this;
    }

    public CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        tag.putDouble("min", min_max.get(true));
        tag.putDouble("max", min_max.get(false));
        tag.putString("type", type.name());
        tag.putString("direction", directionOptional.name());
        return tag;
    }

    public void deserialize(CompoundTag tag){
        try{
            min_max = Couple.create(tag.getDouble("min"), tag.getDouble("max"));
            type = ExposedFieldType.valueOf(tag.getString("type"));
            directionOptional = ExposedFieldDirection.valueOf(tag.getString("direction"));
        }catch (Exception e){
            ControlCraft.LOGGER.info("Some Field didn't get properly deserialized");
        }

    }


    public void apply(double ratio){
        double min = min_max.get(true);
        double max = min_max.get(false);
        field.apply(min + (max - min) * ratio);
    }


    public void reset(){
        min_max = Couple.create(suggestedMinMax.get(true), suggestedMinMax.get(false));
        directionOptional = ExposedFieldDirection.NONE;
    }

}
