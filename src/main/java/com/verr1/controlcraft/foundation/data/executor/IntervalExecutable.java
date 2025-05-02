package com.verr1.controlcraft.foundation.data.executor;

import com.verr1.controlcraft.foundation.api.operatable.Executable;

public class IntervalExecutable implements Executable {

    private final int intervalTicks;
    private int counter = 0;
    private final Runnable runnable;
    private Runnable onExpired = () -> {};

    public IntervalExecutable(Runnable runnable, int intervalTicks, int totalCycles) {
        this.runnable = runnable;
        this.counter = intervalTicks * totalCycles;
        this.intervalTicks = intervalTicks;
    }

    public IntervalExecutable(Runnable runnable, Runnable onExpired, int intervalTicks, int totalCycles) {
        this.runnable = runnable;
        this.counter = intervalTicks * totalCycles;
        this.intervalTicks = intervalTicks;
        this.onExpired = onExpired;
    }


    @Override
    public void run() {
        runnable.run();

    }

    @Override
    public void onRemove() {
        onExpired.run();
    }

    @Override
    public boolean shouldRun() {
        return counter % intervalTicks == 0;
    }

    @Override
    public boolean shouldRemove() {
        return counter <= 0;
    }

    @Override
    public void tick() {
        if(counter > 0)counter--;
    }
}
