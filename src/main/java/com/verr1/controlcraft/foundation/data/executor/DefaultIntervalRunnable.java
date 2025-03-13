package com.verr1.controlcraft.foundation.data.executor;

import com.verr1.controlcraft.foundation.api.IntervalRunnable;

public class DefaultIntervalRunnable implements IntervalRunnable {
    private int cyclesRemained;
    private int intervalTicks;
    private final Runnable runnable;

    public DefaultIntervalRunnable(Runnable runnable, int intervalTicks, int totalCycles) {
        this.runnable = runnable;
        this.intervalTicks = intervalTicks;
        this.cyclesRemained = totalCycles;
    }

    @Override
    public int getCyclesRemained() {
        return cyclesRemained;
    }

    @Override
    public int getIntervalTicks() {
        return intervalTicks;
    }

    @Override
    public void reset() {
        cyclesRemained = intervalTicks;
    }

    @Override
    public void tickDown() {
        intervalTicks--;
    }

    @Override
    public void cycleDown() {
        cyclesRemained--;
    }

    @Override
    public void onExpire() {

    }

    @Override
    public void run() {
        runnable.run();
        cyclesRemained = intervalTicks;
    }
}
