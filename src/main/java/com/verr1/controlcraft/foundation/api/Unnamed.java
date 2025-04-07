package com.verr1.controlcraft.foundation.api;

import net.minecraft.nbt.CompoundTag;

public interface Unnamed<T> {

    static <Q> Unnamed<Q> createEmpty(Class<Q> clazz){
        return new Unnamed<Q>() {
            @Override
            public Q get() {
                return null;
            }

            @Override
            public void set(Q t) {
                // No-op
            }
        };
    }

    T get();
    void set(T t);

}
