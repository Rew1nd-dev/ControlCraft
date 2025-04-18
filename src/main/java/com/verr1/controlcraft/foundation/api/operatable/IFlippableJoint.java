package com.verr1.controlcraft.foundation.api.operatable;

public interface IFlippableJoint {

    default void flip(){setFlipped(!isFlipped());};

    boolean isFlipped();

    void setFlipped(boolean flipped);

}
