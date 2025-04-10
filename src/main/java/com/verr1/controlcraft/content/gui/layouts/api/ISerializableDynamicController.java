package com.verr1.controlcraft.content.gui.layouts.api;

import com.verr1.controlcraft.foundation.data.control.PID;
import net.minecraft.nbt.CompoundTag;

public interface ISerializableDynamicController {

    void PID(PID pid);
    PID PID();

    default CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        PID pid = PID();
        tag.putDouble("p", pid.p());
        tag.putDouble("i", pid.i());
        tag.putDouble("d", pid.d());
        // tag.putDouble("target", target);
        return tag;
    }

    default void deserialize(CompoundTag tag){
        PID(new PID(
                tag.getDouble("p"),
                tag.getDouble("i"),
                tag.getDouble("d")
        ));
        // target = tag.getDouble("target");
    }

}
