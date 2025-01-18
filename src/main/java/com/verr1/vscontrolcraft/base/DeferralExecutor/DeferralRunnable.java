package com.verr1.vscontrolcraft.base.DeferralExecutor;



public interface DeferralRunnable extends Runnable {
    int getDeferralTicks();
    void tickDown();
}
