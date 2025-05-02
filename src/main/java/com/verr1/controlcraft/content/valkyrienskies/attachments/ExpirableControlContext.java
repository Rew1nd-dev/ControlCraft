package com.verr1.controlcraft.content.valkyrienskies.attachments;

import java.util.function.Supplier;

public class ExpirableControlContext<T>{


    private final Supplier<T> contextProvider;
    private int live;
    private final int MAX_LIVE = 10;

    public ExpirableControlContext(Supplier<T>  contextProvider){
        this.contextProvider = contextProvider;
        live = MAX_LIVE;
    }

    public boolean expired(){
        return live <= 0;
    }

    public T context() {
        return contextProvider.get();
    }


    public void tick(){
        if(live > 0){
            live--;
        }
    }
    /*
    public void alive(){
        live = MAX_LIVE;
    }
    * */


}
