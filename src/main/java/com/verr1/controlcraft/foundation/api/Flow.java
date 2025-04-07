package com.verr1.controlcraft.foundation.api;

public interface Flow <L, R>{

    void left(L t);

    void right(R t);

}
