package com.verr1.vscontrolcraft.base;



public interface DeferralRunnable extends Runnable {
    int getDeferralTicks();
    void tickDown();
}
