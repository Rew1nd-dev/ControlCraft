package com.verr1.controlcraft.foundation.api;

public interface QueryConditionRunnable extends DeferralRunnable{


    boolean condition();

    @Override
    default void tickDown() {

    }

    @Override
    default int getDeferralTicks(){
        return condition() ? -1 : 1;
    }
}

