package com.verr1.controlcraft.content.gui.layouts.api;

import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.data.control.PID;
import net.minecraft.nbt.CompoundTag;

public interface ISerializableSchedule {
    NetworkKey SCHEDULE = NetworkKey.create("schedule");

    PID QPID();
    PID PPID();

    void QPID(PID pid);
    void PPID(PID pid);

    default CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        tag.put("QPID", QPID().serialize());
        tag.put("PPID", PPID().serialize());
        return tag;
    }

    default void deserialize(CompoundTag tag){
        QPID(PID.deserialize(tag.getCompound("QPID")));
        PPID(PID.deserialize(tag.getCompound("PPID")));
    }

}
