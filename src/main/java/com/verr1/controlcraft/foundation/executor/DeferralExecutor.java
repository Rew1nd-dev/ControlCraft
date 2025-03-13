package com.verr1.controlcraft.foundation.executor;

import com.verr1.controlcraft.foundation.api.DeferralRunnable;
import com.verr1.controlcraft.foundation.data.executor.DefaultDeferralRunnable;

import java.util.concurrent.ConcurrentLinkedDeque;

public class DeferralExecutor {
    private final ConcurrentLinkedDeque<DeferralRunnable> deferralTasks = new ConcurrentLinkedDeque<>();

    public void tick(){
        deferralTasks.forEach(r -> {
            if(r.getDeferralTicks() <= 0){
                r.run();
            }
            r.tickDown();
        });
        deferralTasks.removeIf(r -> r.getDeferralTicks() <= -1);
    }

    public void executeLater(DeferralRunnable r){
        deferralTasks.add(r);
    }

    public void executeLater(Runnable r, int ticks){
        deferralTasks.add(new DefaultDeferralRunnable(r, ticks));
    }


}
