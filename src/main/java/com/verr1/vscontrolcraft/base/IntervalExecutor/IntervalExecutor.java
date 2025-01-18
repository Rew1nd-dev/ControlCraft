package com.verr1.vscontrolcraft.base.IntervalExecutor;

import java.util.concurrent.ConcurrentLinkedDeque;

public class IntervalExecutor {
    private static ConcurrentLinkedDeque<IntervalRunnable> IntervalTasks = new ConcurrentLinkedDeque<>();

    public static void tick(){
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

    public static void executeOnSchedule(IntervalRunnable r){
        IntervalTasks.add(r);
    }

    public static void executeOnSchedule(Runnable r, int ticks, int cycles){
        IntervalTasks.add(new DefaultIntervalRunnable(r, ticks, cycles));
    }
}
