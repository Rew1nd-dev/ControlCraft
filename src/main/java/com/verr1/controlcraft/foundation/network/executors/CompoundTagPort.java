package com.verr1.controlcraft.foundation.network.executors;

import com.verr1.controlcraft.foundation.api.Slot;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CompoundTagPort implements Slot<CompoundTag> {

    private final Supplier<CompoundTag> supplier;
    private final Consumer<CompoundTag> consumer;

    protected CompoundTagPort(Supplier<CompoundTag> supplier, Consumer<CompoundTag> consumer) {
        this.supplier = supplier;
        this.consumer = consumer;
    }

    public static CompoundTagPort of(Supplier<CompoundTag> supplier, Consumer<CompoundTag> consumer) {
        return new CompoundTagPort(supplier, consumer);
    }


    @Override
    public CompoundTag get() {
        return supplier.get();
    }

    @Override
    public void set(CompoundTag tag) {
        consumer.accept(tag);
    }
}
