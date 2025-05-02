package com.verr1.controlcraft.foundation.executor;

import com.verr1.controlcraft.foundation.api.operatable.Executable;
import com.verr1.controlcraft.foundation.data.executor.DeferralExecutable;
import com.verr1.controlcraft.foundation.data.executor.IntervalExecutable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Executor {

    private final ConcurrentLinkedDeque<Executable> common = new ConcurrentLinkedDeque<>();
    private final ConcurrentHashMap<String, Executable> named = new ConcurrentHashMap<>();


    public void tick(){

        for(var it = common.iterator(); it.hasNext(); ){
            var r = it.next();
            r.tick();
            if(r.shouldRun())r.run();
            if(r.shouldRemove()){
                r.onRemove();
                it.remove();
            }
        }

        for(var it = named.values().iterator(); it.hasNext(); ){
            var r = it.next();
            r.tick();
            if(r.shouldRun())r.run();
            if(r.shouldRemove()){
                r.onRemove();
                it.remove();
            }
        }
    }

    public void execute(Executable task){
        common.add(task);
    }

    public void execute(String name, Executable task){
        named.put(name, task);
    }


    public void executeLater(Runnable task, int tick){
        common.add(new DeferralExecutable(task, tick));
    }

    public void executeOnSchedule(Runnable task, int interval, int cycles){
        common.add(new IntervalExecutable(task, interval, cycles));
    }


    public void executeLater(String name, Runnable task, int tick){
        named.put(name, new DeferralExecutable(task, tick));
    }

    public void executeOnSchedule(String name, Runnable task, int interval, int cycles){
        named.put(name, new IntervalExecutable(task, interval, cycles));
    }


}
