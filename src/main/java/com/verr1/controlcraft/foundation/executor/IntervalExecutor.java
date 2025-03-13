package com.verr1.controlcraft.foundation.executor;

import com.verr1.controlcraft.foundation.api.IntervalRunnable;
import com.verr1.controlcraft.foundation.data.executor.DefaultIntervalRunnable;

import java.util.concurrent.ConcurrentLinkedDeque;

public class IntervalExecutor {
    private final ConcurrentLinkedDeque<IntervalRunnable> IntervalTasks = new ConcurrentLinkedDeque<>();

    public void tick(){
        IntervalTasks.forEach(r -> {
            if(r.getIntervalTicks() <= 0){
                r.run();
                r.reset();
                r.cycleDown();
            }
            r.tickDown();
        });
        IntervalTasks
                .stream()
                .filter(r -> r.getCyclesRemained() <= -1)
                .forEach(IntervalRunnable::onExpire);
        IntervalTasks.removeIf(r -> r.getCyclesRemained() <= -1);
    }

    public void executeOnSchedule(IntervalRunnable r){
        IntervalTasks.add(r);
    }

    public void executeOnSchedule(Runnable r, int ticks, int cycles){
        IntervalTasks.add(new DefaultIntervalRunnable(r, ticks, cycles));
    }
}
