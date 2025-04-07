package com.verr1.controlcraft.foundation.api;

public interface Executable extends Runnable{

    boolean shouldRun();

    boolean shouldRemove();

    void tick();

}
