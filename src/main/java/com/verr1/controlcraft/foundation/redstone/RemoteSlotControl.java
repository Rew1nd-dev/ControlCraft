package com.verr1.controlcraft.foundation.redstone;

import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.foundation.data.NumericField;
import com.verr1.controlcraft.foundation.type.descriptive.SlotDirection;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import com.verr1.controlcraft.utils.CompoundTagBuilder;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.nbt.CompoundTag;

public class RemoteSlotControl {

    public boolean enabled = false;
    public Couple<Double> min_max = Couple.create(0.0, 1.0);
    public Couple<Double> suggestedMinMax = Couple.create(0.0, 1.0);

    private double latestRatio = 0;

    public SlotType type = SlotType.NONE;


    public NumericField mutableField;
    public int groupId = -1;


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





    public RemoteSlotControl suggest(Couple<Double> mm, boolean isBoolean){
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
                .withCompound("enabled", SerializeUtils.BOOLEAN.serialize(enabled))
                .build();
    }

    public void deserialize(CompoundTag tag){
        try{
            type = SerializeUtils.ofEnum(SlotType.class).deserialize(tag.getCompound("type"));
            enabled = SerializeUtils.BOOLEAN.deserialize(tag.getCompound("enabled"));
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
        min_max = Couple.create(suggestedMinMax.get(true), suggestedMinMax.get(false));
        enabled = false;
    }

}
