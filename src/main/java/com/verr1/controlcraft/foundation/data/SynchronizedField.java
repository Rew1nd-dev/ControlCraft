package com.verr1.controlcraft.foundation.data;

public class SynchronizedField<T> {
    T data;
    Object lock = new Object();

    public SynchronizedField(T data){
        this.data = data;
    }

    public void write(T data){
        synchronized (lock){
            this.data = data;
        }
    }

    public T read(){
        synchronized (lock){
            return data;
        }
    }
}
