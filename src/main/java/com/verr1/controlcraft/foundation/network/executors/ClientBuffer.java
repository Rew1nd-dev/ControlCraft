package com.verr1.controlcraft.foundation.network.executors;

import com.verr1.controlcraft.foundation.api.Unnamed;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientBuffer<T> implements Unnamed<CompoundTag> {

    @Nullable
    T buffer;
    Class<T> clazz;

    boolean isDirty = false; // true if 2 consecutive read without a set(tag) in between

    @NotNull
    SerializeUtils.Serializer<T> serializer;

    public ClientBuffer(SerializeUtils.@NotNull Serializer<T> serializer, Class<T> clazz) {
        this.serializer = serializer;
        this.clazz = clazz;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    @Nullable
    public T getBuffer() {
        return buffer;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(){
        isDirty = true;
    }

    public void setBuffer(@Nullable T buffer) {
        this.buffer = buffer;
    }

    @Override
    public CompoundTag get() {
        return serializer.serializeNullable(buffer);
    }

    @Override
    public void set(CompoundTag tag) {
        isDirty = false;
        buffer = serializer.deserialize(tag);
    }
}
