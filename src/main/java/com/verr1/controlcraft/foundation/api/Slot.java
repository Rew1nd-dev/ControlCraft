package com.verr1.controlcraft.foundation.api;

public interface Slot<T> {

    static <Q> Slot<Q> createEmpty(Class<Q> clazz){
        return new Slot<Q>() {
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
