package com.verr1.controlcraft.foundation.executor.executables;

import com.verr1.controlcraft.foundation.api.operatable.Executable;
import org.jetbrains.annotations.NotNull;

public class DeferralExecutable implements Executable {
    private int counter = 0;
    private Runnable task = () -> {};

    public DeferralExecutable(@NotNull Runnable task, int deferralTicks){
        this.counter = deferralTicks;
        this.task = task;
    }

    @Override
    public boolean shouldRun() {
        return counter == 0;
    }

    @Override
    public boolean shouldRemove() {
        return counter <= 0;
    }

    @Override
    public void tick() {
        if(counter >= 0)counter--;
    }

    @Override
    public void run() {
        task.run();
    }
}
