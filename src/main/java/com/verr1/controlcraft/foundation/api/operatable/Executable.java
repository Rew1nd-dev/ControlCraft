package com.verr1.controlcraft.foundation.api.operatable;


/*
*   tick() --> shouldRun() ? run() : __ --> shouldRemove() --> tick()
*
* */

public interface Executable extends Runnable{

    boolean shouldRun();

    boolean shouldRemove();

    default void onRemove(){};

    void tick();

}
