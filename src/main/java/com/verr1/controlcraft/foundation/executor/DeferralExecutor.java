package com.verr1.controlcraft.foundation.executor;

import com.verr1.controlcraft.foundation.api.DeferralRunnable;
import com.verr1.controlcraft.foundation.data.executor.DefaultDeferralRunnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DeferralExecutor {
    private final ConcurrentLinkedDeque<DeferralRunnable> common = new ConcurrentLinkedDeque<>();
    private final ConcurrentHashMap<String, DeferralRunnable> named = new ConcurrentHashMap<>();

    public void tick(){
        common.forEach(r -> {
            if(r.getDeferralTicks() <= 0){
                r.run();
            }
            r.tick();
        });
        common.removeIf(r -> r.getDeferralTicks() <= -1);

        named.values().forEach(r -> {
            if(r.getDeferralTicks() <= 0){
                r.run();
            }
            r.tick();
        });
        named.entrySet().removeIf(e -> e.getValue().getDeferralTicks() <= -1);

    }

    public void executeLater(DeferralRunnable r){
        common.add(r);
    }

    public void executeLater(Runnable r, int ticks){
        common.add(new DefaultDeferralRunnable(r, ticks));
    }

    public void executeLater(DeferralRunnable r, String slot){
        named.put(slot, r);
    }

    public void executeLater(Runnable r, int ticks, String slot){
        named.put(slot, new DefaultDeferralRunnable(r, ticks));
    }


}
