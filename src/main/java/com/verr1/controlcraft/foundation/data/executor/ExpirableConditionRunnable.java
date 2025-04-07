package com.verr1.controlcraft.foundation.data.executor;

import com.verr1.controlcraft.foundation.api.DeferralRunnable;

import java.util.function.Supplier;

public class ExpirableConditionRunnable implements DeferralRunnable {
    private final Runnable orElse;
    private final Supplier<Boolean> condition;
    private boolean lastConditionResult = false;
    private int expirationTicks;
    private final Runnable task;
    private boolean taskExecuted = false;

    public ExpirableConditionRunnable(
            Supplier<Boolean> condition,
            Runnable task,
            Runnable orElse,
            int expirationTicks
    ) {
        this.condition = condition;
        this.expirationTicks = expirationTicks;
        this.orElse = orElse;
        this.task = task;
    }

    @Override
    public int getDeferralTicks() {
        if(taskExecuted)return -1;
        return (lastConditionResult || expirationTicks < 0) ? 0 : 1;
    }

    @Override
    public void tick() {
        lastConditionResult = condition.get();
        if(expirationTicks >= 0)expirationTicks--;
    }

    @Override
    public void run() {
        if(lastConditionResult)task.run();
        else if(expirationTicks <= 0) orElse.run();
        taskExecuted = true;
    }

    public static class builder{

        private Supplier<Boolean> condition = () -> false;
        private int expirationTicks = 10;
        private final Runnable task;
        private Runnable orElse = () -> {};

        public builder(Runnable task){
            this.task = task;
        }

        public builder withCondition(Supplier<Boolean> condition){
            this.condition = condition;
            return this;
        }

        public builder withExpirationTicks(int expirationTicks){
            this.expirationTicks = expirationTicks;
            return this;
        }

        public builder withOrElse(Runnable orElse){
            this.orElse = orElse;
            return this;
        }

        public ExpirableConditionRunnable build(){
            return new ExpirableConditionRunnable(
                    condition,
                    task,
                    orElse,
                    expirationTicks
            );
        }
    }


}
