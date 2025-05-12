package com.verr1.controlcraft.foundation.redstone;

import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.foundation.data.NumericField;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.type.descriptive.SlotDirection;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import com.verr1.controlcraft.utils.CompoundTagBuilder;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.nbt.CompoundTag;

public class DirectSlotControl {

    public static ExposedFieldWrapper EMPTY = new ExposedFieldWrapper(NumericField.EMPTY, SlotType.NONE);


    public Couple<Double> min_max = Couple.create(0.0, 1.0);
    public Couple<Double> suggestedMinMax = Couple.create(0.0, 1.0);
    public SlotDirection direction = SlotDirection.NONE;

    private double latestRatio = 0;

    private SlotType type;

    public double valueOf(double ratio){
        double min = min_max.get(true);
        double max = min_max.get(false);
        return min + (max - min) * ratio;
    }

    public double latest(){
        return latestRatio;
    }

    public double latestValue(){
        return valueOf(latest());
    }

    public void store(double ratio){
        latestRatio = ratio;
    }



    public SlotType type() {
        return type;
    }





    public DirectSlotControl(SlotType type) {
        this.type = type;
    }


    public DirectSlotControl suggest(Couple<Double> mm, boolean isBoolean){
        if(isBoolean)return this;
        this.min_max = mm;
        suggestedMinMax = mm;

        return this;
    }

    public CompoundTag serialize(){
        return CompoundTagBuilder.create()
                .withCompound("min", SerializeUtils.DOUBLE.serialize(min_max.get(true)))
                .withCompound("max", SerializeUtils.DOUBLE.serialize(min_max.get(false)))
                .withCompound("type", SerializeUtils.ofEnum(SlotType.class).serialize(type))
                .withCompound("direction", SerializeUtils.ofEnum(SlotDirection.class).serialize(direction))
                .build();
    }

    public void deserialize(CompoundTag tag){
        if(tag.isEmpty())return;
        try{

            type = SerializeUtils.ofEnum(SlotType.class).deserialize(tag.getCompound("type"));
            direction = SerializeUtils.ofEnum(SlotDirection.class).deserialize(tag.getCompound("direction"));
            min_max = type.isBoolean() ?
                    Couple.create(0.0, 1.0) :
                    Couple.create(
                        SerializeUtils.DOUBLE.deserialize(tag.getCompound("min")),
                        SerializeUtils.DOUBLE.deserialize(tag.getCompound("max"))
                    );
        }catch (Exception e){
            ControlCraft.LOGGER.info("Some Slot didn't get properly deserialized");
        }

    }


    public void reset(){
        direction = SlotDirection.NONE;
        if(type.isBoolean())return;
        min_max = Couple.create(suggestedMinMax.get(true), suggestedMinMax.get(false));
    }

}
