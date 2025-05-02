package com.verr1.controlcraft.foundation.network.executors;

import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SerializePort<T> extends CompoundTagPort {

    private SerializePort(Supplier<T> supplier, Consumer<T> consumer, SerializeUtils.Serializer<T> serializer) {
        super(() -> serializer.serialize(supplier.get()), tag -> consumer.accept(serializer.deserialize(tag)));
    }

    public static <T> SerializePort<T> of(
            Supplier<T> supplier,
            Consumer<T> consumer,
            SerializeUtils.Serializer<T> serializer
    ) {
        return new SerializePort<>(supplier, consumer, serializer);
    }


}
