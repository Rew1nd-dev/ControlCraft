package com.verr1.vscontrolcraft.base.DeferralExecutor;

public class DefaultDeferralRunnable implements DeferralRunnable {
    int ticks = 0;
    Runnable task;

    public DefaultDeferralRunnable(Runnable task, int ticks){
        this.ticks = ticks;
        this.task = task;
    }

    @Override
    public int getDeferralTicks() {
        return ticks;
    }

    @Override
    public void tickDown() {
        ticks--;
    }

    @Override
    public void run() {
        task.run();
    }
}