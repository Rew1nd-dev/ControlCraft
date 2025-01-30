package com.verr1.vscontrolcraft.base.DeferralExecutor;

import java.util.concurrent.ConcurrentLinkedDeque;

public class DeferralExecutor {
    private static final ConcurrentLinkedDeque<DeferralRunnable> deferralTasks = new ConcurrentLinkedDeque<>();

    public static void tick(){
        deferralTasks.forEach(r -> {
            if(r.getDeferralTicks() <= 0){
                r.run();
            }
            r.tickDown();
        });
        deferralTasks.removeIf(r -> r.getDeferralTicks() <= -1);
    }

    public static void executeLater(DeferralRunnable r){
        deferralTasks.add(r);
    }

    public static void executeLater(Runnable r, int ticks){
        deferralTasks.add(new DefaultDeferralRunnable(r, ticks));
    }


}
