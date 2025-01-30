package com.verr1.vscontrolcraft.base;

import com.verr1.vscontrolcraft.ControlCraft;

import java.util.function.UnaryOperator;

public class ExpirableData<T>{
    private T data;
    private int life;
    private int MAX_LIFE = 0;
    private boolean isExpired = false;

    private UnaryOperator<T> onExpire;

    public void tick(){
        if(life > 0){
            life--;
        } else if(!isExpired){
            isExpired = true;
            try{
                data = onExpire.apply(data);
            }catch (Exception e){
                ControlCraft.LOGGER.error("Error on ExpirableData.onExpire: " + e.getMessage());
            }

        }
    }

    public T data(){
        return data;
    }

    public void alive(){
        life = MAX_LIFE;
    }

    public boolean isExpired(){
        return isExpired;
    }

    public void forceExpire(){
        isExpired = true;
        try{
            data = onExpire.apply(data);
        }catch (Exception e){
            ControlCraft.LOGGER.error("Error on ExpirableData.onExpire: " + e.getMessage());
        }
    }

    public ExpirableData(T data, int life, UnaryOperator<T> onExpire){
        this.data = data;
        this.life = life;
        this.MAX_LIFE = life;
        this.onExpire = onExpire;
    }

}