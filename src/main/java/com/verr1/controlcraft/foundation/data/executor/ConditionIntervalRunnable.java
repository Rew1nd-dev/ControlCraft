package com.verr1.controlcraft.foundation.data.executor;

import com.verr1.controlcraft.foundation.api.IntervalRunnable;

import java.util.function.Supplier;

public class ConditionIntervalRunnable implements IntervalRunnable {

    private final Supplier<Boolean> condition;
    private final Runnable task;
    private final Runnable onConditionMeet;

    ConditionIntervalRunnable(Supplier<Boolean> condition, Runnable task, Runnable onConditionMeet) {
        this.condition = condition;
        this.task = task;
        this.onConditionMeet = onConditionMeet;
    }


    @Override
    public int getCyclesRemained() {
        return condition.get() ? -1 : 1;
    }

    @Override
    public int getIntervalTicks() {
        return 0;
    }

    @Override
    public void reset() {

    }

    @Override
    public void tickDown() {

    }

    @Override
    public void cycleDown() {

    }

    @Override
    public void onExpire() {
        onConditionMeet.run();
    }

    @Override
    public void run() {
        task.run();
    }

    public static class builder{
        private Supplier<Boolean> condition = () -> false;
        private final Runnable task;
        private Runnable onConditionMeet = () -> {};

        public builder(Runnable task){
            this.task = task;
        }

        public builder withCondition(Supplier<Boolean> condition){
            this.condition = condition;
            return this;
        }

        public builder withOnConditionMeet(Runnable onConditionMeet){
            this.onConditionMeet = onConditionMeet;
            return this;
        }

        public ConditionIntervalRunnable build(){
            return new ConditionIntervalRunnable(condition, task, onConditionMeet);
        }
    }
}
