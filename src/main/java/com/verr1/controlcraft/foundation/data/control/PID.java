package com.verr1.controlcraft.foundation.data.control;

import net.minecraft.nbt.CompoundTag;

public record PID (double p, double i, double d) {

    public static PID EMPTY = new PID(0, 0, 0);

    public CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        tag.putDouble("P", p);
        tag.putDouble("I", i);
        tag.putDouble("D", d);
        return tag;
    }

    public static PID deserialize(CompoundTag tag){
        return new PID(
                tag.getDouble("P"),
                tag.getDouble("I"),
                tag.getDouble("D")
        );
    }

}
