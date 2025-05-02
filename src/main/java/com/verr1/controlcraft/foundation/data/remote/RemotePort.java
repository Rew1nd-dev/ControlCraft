package com.verr1.controlcraft.foundation.data.remote;

import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;
import java.util.function.Function;

/*
*   T is task input
* */

public class RemotePort<T> {
    public Class<T> type;

    public Consumer<T> task;

    public Function<T, CompoundTag> serializer;

    public Function<CompoundTag, T> deserializer;

    public RemotePort(Class<T> type, Consumer<T> task, Function<T, CompoundTag> serializer, Function<CompoundTag, T> deserializer) {
        this.type = type;
        this.task = task;
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public static<T> RemotePort<T> of(Class<T> type, Consumer<T> task, SerializeUtils.Serializer<T> serializer) {
        return new RemotePort<>(
                type,
                task,
                serializer::serializeNullable,
                serializer::deserializeNullable
        );
    }

    public CompoundTag serialize(Object input) {
        if (!type.isInstance(input)) {
            return new CompoundTag();
        }
        return serializer.apply(type.cast(input));
    }

    public void accept(Object object){
        if (!type.isInstance(object)) {
            return;
        }
        task.accept(type.cast(object));
    }

    public T deserialize(CompoundTag tag) {
        return deserializer.apply(tag);
    }
}
