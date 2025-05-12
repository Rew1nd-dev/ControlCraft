package com.verr1.controlcraft.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.UUID;

public class CompoundTagBuilder {

    private final CompoundTag toBuild = new CompoundTag();

    public static CompoundTagBuilder create(){
        return new CompoundTagBuilder();
    }

    public CompoundTagBuilder withString(String name, String content){
        toBuild.putString(name, content);
        return this;
    }

    public CompoundTagBuilder withLong(String name, Long content){
        toBuild.putLong(name, content);
        return this;
    }

    public CompoundTagBuilder withCompound(String name, CompoundTag content){
        toBuild.put(name, content);
        return this;
    }

    public CompoundTagBuilder withListTag(String name, ListTag content){
        toBuild.put(name, content);
        return this;
    }

    public CompoundTagBuilder withUUID(String name, UUID uuid){
        toBuild.putUUID(name, uuid);
        return this;
    }

    public CompoundTag build(){
        return toBuild;
    }

}
