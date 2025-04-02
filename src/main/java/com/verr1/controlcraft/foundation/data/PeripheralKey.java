package com.verr1.controlcraft.foundation.data;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public record PeripheralKey(String Name, long Protocol) {
    public static PeripheralKey NULL = new PeripheralKey("null", 0);
    @Override
    public int hashCode() {
        return Long.hashCode(Protocol);
    }

    public CompoundTag serialize(){
        CompoundTag tag = new CompoundTag();
        tag.putString("name", Name);
        tag.putLong("protocol", Protocol);
        return tag;
    }

    public static @NotNull PeripheralKey deserialize(CompoundTag tag){
        if (tag == null) return NULL;
        if(!(tag.contains("name") && tag.contains("protocol")))return NULL;
        return new PeripheralKey(tag.getString("name"), tag.getLong("protocol"));
    }
}
