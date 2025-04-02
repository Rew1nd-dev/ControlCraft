package com.verr1.controlcraft.foundation.data.executor;

import com.verr1.controlcraft.foundation.api.DeferralRunnable;

public class DefaultDeferralRunnable implements DeferralRunnable {
    int ticks = 0;
    final Runnable task;

    public DefaultDeferralRunnable(Runnable task, int ticks){
        this.ticks = ticks;
        this.task = task;
    }

    @Override
    public int getDeferralTicks() {
        return ticks;
    }

    @Override
    public void tick() {
        ticks--;
    }

    @Override
    public void run() {
        task.run();
    }
}
