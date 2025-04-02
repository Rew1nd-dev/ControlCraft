package com.verr1.controlcraft.foundation.network;

public class SyncLock {

    private boolean isLocked = false;
    private int autoResetTimer = 10;
    private boolean updated = false;

    public void activate(){
        isLocked = true;
        autoResetTimer = 10;
    }

    public void update(){
        updated = true;
    }

    public void setDirty(){
        updated = false;
    }

    public boolean isUpdated(){
        return updated;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void tick(){
        if(autoResetTimer > 0){
            autoResetTimer--;
        }else{
            isLocked = false;
        }
    }

}
