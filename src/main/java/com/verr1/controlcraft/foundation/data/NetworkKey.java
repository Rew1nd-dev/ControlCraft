package com.verr1.controlcraft.foundation.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class NetworkKey implements StringRepresentable {
    private static final Map<String, NetworkKey> registry = new HashMap<>();

    private final String key;

    private int permissionLevel = 0;

    private NetworkKey(String key){
        this.key = key;
    }

    private NetworkKey(String key, int permissionLevel){
        this.key = key;
        this.permissionLevel = permissionLevel;
    }

    public static NetworkKey create(String key){
        // if (registry.containsKey(key))throw new IllegalStateException("duplicate registration");

        return registry.computeIfAbsent(key, k -> new NetworkKey(key));
    }

    public static NetworkKey create(String key, int permissionLevel){
        // if (registry.containsKey(key))throw new IllegalStateException("duplicate registration");

        return registry.computeIfAbsent(key, k -> new NetworkKey(key, permissionLevel));
    }


    public CompoundTag serialize(){
        CompoundTag t = new CompoundTag();
        t.putString("key", key);
        return t;
    }

    public int permissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(int permissionLevel) {
        this.permissionLevel = permissionLevel;
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
