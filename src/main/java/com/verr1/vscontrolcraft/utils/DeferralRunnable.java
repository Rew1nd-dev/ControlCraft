package com.verr1.vscontrolcraft.utils;



public interface DeferralRunnable extends Runnable {
    int getDeferralTicks();
    void tickDown();
}
