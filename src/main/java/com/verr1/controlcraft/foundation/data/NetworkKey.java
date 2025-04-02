package com.verr1.controlcraft.foundation.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class NetworkKey implements StringRepresentable {
    private final String key;

    private NetworkKey(String key){
        this.key = key;
    }

    public static NetworkKey create(String key){
        return new NetworkKey(key);
    }

    public CompoundTag serialize(){
        CompoundTag t = new CompoundTag();
        t.putString("key", key);
        return t;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof NetworkKey key_))return false;
        return key.equals(key_.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public static NetworkKey deserialize(CompoundTag t){
        return new NetworkKey(t.getString("key"));
    }

    @Override
    public @NotNull String getSerializedName() {
        return key;
    }
}
