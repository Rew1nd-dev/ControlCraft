package com.verr1.controlcraft.foundation.network.executors;

import com.verr1.controlcraft.foundation.api.Unnamed;
import com.verr1.controlcraft.foundation.data.constraint.ConnectContext;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.function.Supplier;

public class ClientBuffer<T> implements Unnamed<CompoundTag> {

    public static Supplier<ClientBuffer<Double>> DOUBLE = () -> new ClientBuffer<>(SerializeUtils.DOUBLE, Double.class);
    public static Supplier<ClientBuffer<Float>> FLOAT = () -> new ClientBuffer<>(SerializeUtils.FLOAT, Float.class);
    public static Supplier<ClientBuffer<Boolean>> BOOLEAN = () -> new ClientBuffer<>(SerializeUtils.BOOLEAN, Boolean.class);
    public static Supplier<ClientBuffer<String>> STRING = () -> new ClientBuffer<>(SerializeUtils.STRING, String.class);
    public static Supplier<ClientBuffer<Integer>> INT = () -> new ClientBuffer<>(SerializeUtils.INT, Integer.class);
    public static Supplier<ClientBuffer<Long>> LONG = () -> new ClientBuffer<>(SerializeUtils.LONG, Long.class);
    public static Supplier<ClientBuffer<Vector3d>> VECTOR3D = () -> new ClientBuffer<>(SerializeUtils.VECTOR3D, Vector3d.class);
    public static Supplier<ClientBuffer<ConnectContext>> CONNECT_CONTEXT = () -> new ClientBuffer<>(SerializeUtils.CONNECT_CONTEXT, ConnectContext.class);


    @Nullable
    T buffer;
    Class<T> clazz;

    boolean isDirty = false; // true if 2 consecutive read without a set(tag) in between

    @NotNull
    SerializeUtils.Serializer<T> serializer;

    public ClientBuffer(SerializeUtils.@NotNull Serializer<T> serializer, Class<T> clazz) {
        this.serializer = serializer;
        this.clazz = clazz;
    }

    public static<T extends Enum<?>> ClientBuffer<T> of(Class<T> enumClass){
        return new ClientBuffer<>(SerializeUtils.ofEnum(enumClass), enumClass);
    }

    public Class<T> getClazz() {
        return clazz;
    }

    @Nullable
    public T getBuffer() {
        return buffer;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(){
        isDirty = true;
    }

    public void setBuffer(@Nullable T buffer) {
        this.buffer = buffer;
    }

    @Override
    public CompoundTag get() {
        return serializer.serializeNullable(buffer);
    }

    @Override
    public void set(CompoundTag tag) {
        isDirty = false;
        buffer = serializer.deserialize(tag);
    }
}
