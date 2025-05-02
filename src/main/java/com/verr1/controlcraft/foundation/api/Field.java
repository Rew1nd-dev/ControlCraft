package com.verr1.controlcraft.foundation.api;

public interface Field<T> {


    T value();

    void apply(T value);

    String name();


}
