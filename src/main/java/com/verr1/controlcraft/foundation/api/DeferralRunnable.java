package com.verr1.controlcraft.foundation.api;

public interface DeferralRunnable extends Runnable {

    int getDeferralTicks();

    void tick();
}
