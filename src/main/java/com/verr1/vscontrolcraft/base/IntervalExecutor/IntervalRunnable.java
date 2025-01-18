package com.verr1.vscontrolcraft.base.IntervalExecutor;

public interface IntervalRunnable extends Runnable{
    int getCyclesRemained();
    int getIntervalTicks();
    void reset();
    void tickDown();
    void cycleDown();
    void onExpire();
}
