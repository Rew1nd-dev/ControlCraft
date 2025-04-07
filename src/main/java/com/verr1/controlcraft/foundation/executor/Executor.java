package com.verr1.controlcraft.foundation.executor;

import com.verr1.controlcraft.foundation.api.DeferralRunnable;
import com.verr1.controlcraft.foundation.api.Executable;
import com.verr1.controlcraft.foundation.data.executor.DefaultDeferralRunnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Executor {

    private final ConcurrentLinkedDeque<Executable> common = new ConcurrentLinkedDeque<>();
    private final ConcurrentHashMap<String, Executable> named = new ConcurrentHashMap<>();


    public void tick(){
        common.forEach(r -> {
            r.tick();
            if(r.shouldRun()){
                r.run();
            }
        });
        common.removeIf(Executable::shouldRemove);

        if(named.isEmpty())return;
        named.values().forEach(r -> {
            r.tick();
            if(r.shouldRun()){
                r.run();
            }
        });
        named.entrySet().removeIf(e -> e.getValue().shouldRemove());

    }

    public void execute(Executable task){
        common.add(task);
    }

    public void execute(String name, Executable task){
        named.put(name, task);
    }


}
