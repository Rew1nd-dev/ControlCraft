package com.verr1.vscontrolcraft.base.DeferralExecutor;

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
