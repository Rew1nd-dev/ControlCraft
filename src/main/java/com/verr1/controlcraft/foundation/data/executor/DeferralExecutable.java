package com.verr1.controlcraft.foundation.data.executor;

import com.verr1.controlcraft.foundation.api.Executable;

public class DeferralExecutable implements Executable {

    private int deferralTicks = 0;
    private Runnable task = () -> {};

    public DeferralExecutable(int deferralTicks, Runnable task) {
        this.deferralTicks = deferralTicks;
        this.task = task;
    }


    @Override
    public boolean shouldRun() {
        return deferralTicks == 0;
    }

    @Override
    public boolean shouldRemove() {
        return deferralTicks < 0;
    }

    @Override
    public void tick() {
        if(deferralTicks > -1)deferralTicks--;
    }

    @Override
    public void run() {
        task.run();
    }
}
