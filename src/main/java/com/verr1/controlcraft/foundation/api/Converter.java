package com.verr1.controlcraft.foundation.api;

public interface Converter <L, R> extends Flow<L, R> {

    R forward(L v);

    L backward(R v);

    default void left(L t) {
        forward(t);
    }

    default void right(R t) {
        backward(t);
    }

}
