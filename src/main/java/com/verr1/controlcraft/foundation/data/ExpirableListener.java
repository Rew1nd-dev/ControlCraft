package com.verr1.controlcraft.foundation.data;

import java.util.function.Consumer;

public class ExpirableListener<E> {
    private final Consumer<E> consumer;
    private final int MAX_LIVE ;
    private int live;

    public ExpirableListener(Consumer<E> consumer, int max_live) {
        this.consumer = consumer;
        this.MAX_LIVE = max_live;
        this.live = max_live;
    }

    public void tick(){
        if(live > 0)live--;
    }

    public void accept(E e){
        consumer.accept(e);
    }

    public void reset(){
        live = MAX_LIVE;
    }

    public boolean isExpired(){
        return live <= 0;
    }

}
