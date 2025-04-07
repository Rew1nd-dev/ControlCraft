package com.verr1.controlcraft.foundation.data.executor;

import com.verr1.controlcraft.foundation.api.Executable;

import java.util.function.Supplier;

public class ConditionExecutable implements Executable {

    private Supplier<Boolean> condition = () -> false;
    private int expirationTicks;
    private final Runnable task;
    private Runnable orElse = () -> {};

    private boolean latest = false;

    public ConditionExecutable(Runnable orElse, Runnable task, int expirationTicks, Supplier<Boolean> condition) {
        this.orElse = orElse;
        this.task = task;
        this.expirationTicks = expirationTicks;
        this.condition = condition;
    }

    @Override
    public boolean shouldRun() {
        return latest || expirationTicks == 0;
    }

    @Override
    public boolean shouldRemove() {
        return expirationTicks < -1;
    }

    @Override
    public void tick() {
        latest = condition.get();
        expirationTicks--;
    }

    @Override
    public void run() {
        if(latest)task.run();
        else if(expirationTicks == 0)orElse.run();
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

        public ConditionExecutable build(){
            return new ConditionExecutable(
                    orElse,
                    task,
                    expirationTicks,
                    condition
            );
        }
    }

}
