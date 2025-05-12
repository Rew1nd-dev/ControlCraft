package com.verr1.controlcraft.utils;

import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.data.constraint.ConnectContext;
import com.verr1.controlcraft.foundation.network.SyncLock;
import com.verr1.controlcraft.foundation.vsapi.VSJointPose;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.apigame.constraints.*;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class SerializeUtils {
    public static HashMap<Class<?>, Serializer<?>> EnumSerializerCache = new HashMap<>();

    public static Serializer<Double> DOUBLE = of(SerializeUtils::ofDouble, tag -> tag.getDouble("value"));
    public static Serializer<Float> FLOAT = of(SerializeUtils::ofFloat, tag -> tag.getFloat("value"));
    public static Serializer<Integer> INT = of(SerializeUtils::ofInt, tag -> tag.getInt("value"));
    public static Serializer<Long> LONG = of(SerializeUtils::ofLong, tag -> tag.getLong("value"));
    public static Serializer<Boolean> BOOLEAN = of(SerializeUtils::ofBoolean, tag -> tag.getBoolean("value"));
    public static Serializer<String> STRING = of(SerializeUtils::ofString, tag -> tag.getString("value"));
    public static Serializer<CompoundTag> UNIT = of(tag -> tag, tag -> tag);

    public static Serializer<Vector3dc> VECTOR3DC = of(
            vec -> {
                CompoundTag tag = new CompoundTag();
                tag.putDouble("x", vec.x());
                tag.putDouble("y", vec.y());
                tag.putDouble("z", vec.z());
                return tag;
            },
            tag -> new Vector3d(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"))
    );
    public static Serializer<Quaterniondc> QUATERNION4DC = of(
            quat -> {
                CompoundTag tag = new CompoundTag();
                tag.putDouble("x", quat.x());
                tag.putDouble("y", quat.y());
                tag.putDouble("z", quat.z());
                tag.putDouble("w", quat.w());
                return tag;
            },
            tag -> new Quaterniond(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"), tag.getDouble("w"))
    );

    public static Serializer<Vector3d> VECTOR3D = of(
            vec -> {
                CompoundTag tag = new CompoundTag();
                tag.putDouble("x", vec.x());
                tag.putDouble("y", vec.y());
                tag.putDouble("z", vec.z());
                return tag;
            },
            tag -> new Vector3d(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"))
    );
    public static Serializer<Quaterniond> QUATERNION4D = of(
            quat -> {
                CompoundTag tag = new CompoundTag();
                tag.putDouble("x", quat.x());
                tag.putDouble("y", quat.y());
                tag.putDouble("z", quat.z());
                tag.putDouble("w", quat.w());
                return tag;
            },
            tag -> new Quaterniond(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"), tag.getDouble("w"))
    );

    public static<T, A, C extends Collection<T>> Serializer<Collection<T>> ofCollection(Serializer<T> elementSerializer, Collector<T, A, C> collector){
        return new Serializer<>() {
            @Override
            public CompoundTag serialize(@NotNull Collection<T> obj) {

                AtomicInteger count = new AtomicInteger();
                CompoundTagBuilder builder = new CompoundTagBuilder().withLong("count", (long) obj.size());
                obj.forEach(d -> builder.withCompound("element_" + count.getAndIncrement(), elementSerializer.serialize(d)));
                return builder.build();

            }

            @Override
            public @NotNull Collection<T> deserialize(CompoundTag tag) {
                A container = collector.supplier().get();
                long count = tag.getLong("count");
                for (long i = 0; i < count; i++) {
                    CompoundTag elementTag = tag.getCompound("element_" + i);
                    T element = elementSerializer.deserialize(elementTag);
                    collector.accumulator().accept(container, element);
                }
                return collector.finisher().apply(container);
            }
        };
    }

    @SuppressWarnings("unchecked") // It's checked
    public static<T extends Enum<?>> Serializer<T> ofEnum(Class<T> enumClazz){
        return (Serializer<T>)EnumSerializerCache.computeIfAbsent(
                enumClazz,
                clazz_ ->
                        new Serializer<T>() {
                            final Class<T> clazz = enumClazz;

                            @Override
                            public CompoundTag serialize(@NotNull T obj) {
                                return INT.serialize(obj.ordinal());
                            }

                            @Override
                            public @NotNull T deserialize(CompoundTag tag) {
                                int ordinal = INT.deserialize(tag);
                                T validValue = clazz.getEnumConstants()[0];
                                try{
                                    validValue = clazz.getEnumConstants()[ordinal];
                                }catch (IndexOutOfBoundsException e){
                                    ControlCraft.LOGGER.error("receive ordinal: {}, but class {} does not contain that much elements", ordinal, clazz);
                                }
                                return validValue;
                            }
                        }
                );

    }



    public static Serializer<VSJointPose> JOINT_POSE = of(
            pose -> {
                CompoundTag tag = new CompoundTag();
                tag.put("p", VECTOR3DC.serialize(pose.getPos()));
                tag.put("q", QUATERNION4DC.serialize(pose.getRot()));
                return tag;
            },
            tag -> new VSJointPose(
                    VECTOR3DC.deserialize(tag.getCompound("p")),
                    QUATERNION4DC.deserialize(tag.getCompound("q")))
    );

    public static Serializer<ConnectContext> CONNECT_CONTEXT = of(
            context -> {
                CompoundTag tag = new CompoundTag();
                tag.put("self", JOINT_POSE.serialize(context.self()));
                tag.put("comp", JOINT_POSE.serialize(context.comp()));
                tag.put("dirty", BOOLEAN.serialize(context.isDirty()));
                return tag;
            },
            tag -> new ConnectContext(
                    JOINT_POSE.deserialize(tag.getCompound("self")),
                    JOINT_POSE.deserialize(tag.getCompound("comp")),
                    BOOLEAN.deserialize(tag.getCompound("dirty"))
                )
            );


    public static Serializer<VSConstraint> CONSTRAINT = of(
            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("ship_id_0", LONG.serialize(Optional.of(joint.getShipId0()).orElse(-1L)));
                tag.put("ship_id_1", LONG.serialize(Optional.of(joint.getShipId1()).orElse(-1L)));
                tag.put("compliance", DOUBLE.serialize(joint.getCompliance()));
                return tag;
            },
            tag -> null
    );

    public static Serializer<VSAttachmentConstraint> ATTACH = of(
            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("joint", CONSTRAINT.serialize(joint));
                tag.put("p_0", VECTOR3DC.serialize(joint.getLocalPos0()));
                tag.put("p_1", VECTOR3DC.serialize(joint.getLocalPos1()));
                tag.put("m_f", DOUBLE.serialize(joint.getMaxForce()));
                tag.put("d", DOUBLE.serialize(joint.getFixedDistance()));
                return tag;
            },
            tag -> {
                CompoundTag commonJoint = tag.getCompound("joint");
                return new VSAttachmentConstraint(
                        LONG.deserialize(commonJoint.getCompound("ship_id_0")),
                        LONG.deserialize(commonJoint.getCompound("ship_id_1")),
                        DOUBLE.deserialize(commonJoint.getCompound("compliance")),
                        VECTOR3DC.deserialize(tag.getCompound("p_0")),
                        VECTOR3DC.deserialize(tag.getCompound("p_1")),
                        DOUBLE.deserialize(tag.getCompound("m_f")),
                        DOUBLE.deserialize(tag.getCompound("d"))
                );
            }
    );

    public static Serializer<VSHingeOrientationConstraint> ORIENT = of(

            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("joint", CONSTRAINT.serialize(joint));
                tag.put("q_0", QUATERNION4DC.serialize(joint.getLocalRot0()));
                tag.put("q_1", QUATERNION4DC.serialize(joint.getLocalRot1()));
                tag.put("m_t", DOUBLE.serialize(joint.getMaxTorque()));
                return tag;
            },
            tag -> {
                CompoundTag commonJoint = tag.getCompound("joint");
                return new VSHingeOrientationConstraint(
                        LONG.deserialize(commonJoint.getCompound("ship_id_0")),
                        LONG.deserialize(commonJoint.getCompound("ship_id_1")),
                        DOUBLE.deserialize(commonJoint.getCompound("compliance")),
                        QUATERNION4DC.deserialize(tag.getCompound("q_0")),
                        QUATERNION4DC.deserialize(tag.getCompound("q_1")),
                        DOUBLE.deserialize(tag.getCompound("m_t"))
                );
            }
    );

    public static Serializer<VSFixedOrientationConstraint> FIXED_ORIENT = of(
            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("joint", CONSTRAINT.serialize(joint));
                tag.put("q_0", QUATERNION4DC.serialize(joint.getLocalRot0()));
                tag.put("q_1", QUATERNION4DC.serialize(joint.getLocalRot1()));
                tag.put("m_t", DOUBLE.serialize(joint.getMaxTorque()));
                return tag;
            },
            tag -> {
                CompoundTag commonJoint = tag.getCompound("joint");
                return new VSFixedOrientationConstraint(
                        LONG.deserialize(commonJoint.getCompound("ship_id_0")),
                        LONG.deserialize(commonJoint.getCompound("ship_id_1")),
                        DOUBLE.deserialize(commonJoint.getCompound("compliance")),
                        QUATERNION4DC.deserialize(tag.getCompound("q_0")),
                        QUATERNION4DC.deserialize(tag.getCompound("q_1")),
                        DOUBLE.deserialize(tag.getCompound("m_t"))
                );
            }
    );

    public static Serializer<VSSlideConstraint> SLIDE = of(
            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("joint", CONSTRAINT.serialize(joint));
                tag.put("p_0", VECTOR3DC.serialize(joint.getLocalPos0()));
                tag.put("p_1", VECTOR3DC.serialize(joint.getLocalPos1()));
                tag.put("m_f", DOUBLE.serialize(joint.getMaxForce()));
                tag.put("axis", VECTOR3DC.serialize(joint.getLocalSlideAxis0()));
                tag.put("m_d", DOUBLE.serialize(joint.getMaxDistBetweenPoints()));
                return tag;
            },
            tag -> {
                CompoundTag commonJoint = tag.getCompound("joint");
                return new VSSlideConstraint(
                        LONG.deserialize(commonJoint.getCompound("ship_id_0")),
                        LONG.deserialize(commonJoint.getCompound("ship_id_1")),
                        DOUBLE.deserialize(commonJoint.getCompound("compliance")),
                        VECTOR3DC.deserialize(tag.getCompound("p_0")),
                        VECTOR3DC.deserialize(tag.getCompound("p_1")),
                        DOUBLE.deserialize(tag.getCompound("m_f")),
                        VECTOR3DC.deserialize(tag.getCompound("axis")),
                        DOUBLE.deserialize(tag.getCompound("m_d"))
                );
            }
    );




    public static <T> Serializer<T> of(Function<T, CompoundTag> serializer, Function<CompoundTag, T> deserializer){
        return new Serializer<>() {
            @Override
            public CompoundTag serialize(@NotNull T obj) {
                return serializer.apply(obj);
            }

            @Override
            public @NotNull T deserialize(CompoundTag tag) {
                return deserializer.apply(tag);
            }
        };
    }

    public static CompoundTag ofDouble(double d){
        CompoundTag tag = new CompoundTag();
        tag.putDouble("value", d);
        return tag;
    }

    public static CompoundTag ofFloat(float f){
        CompoundTag tag = new CompoundTag();
        tag.putFloat("value", f);
        return tag;
    }

    public static CompoundTag ofInt(int i){
        CompoundTag tag = new CompoundTag();
        tag.putInt("value", i);
        return tag;
    }

    public static CompoundTag ofBoolean(boolean b){
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("value", b);
        return tag;
    }

    public static CompoundTag ofString(String s){
        CompoundTag tag = new CompoundTag();
        tag.putString("value", s);
        return tag;
    }

    public static CompoundTag ofLong(long l){
        CompoundTag tag = new CompoundTag();
        tag.putLong("value", l);
        return tag;
    }

    public interface Serializer<T>{


        default CompoundTag serializeNullable(@Nullable T obj){
            return obj == null ? new CompoundTag() : serialize(obj);
        }

        default @Nullable T deserializeNullable(CompoundTag tag){
            return tag.isEmpty() ? null : deserialize(tag);
        }

        CompoundTag serialize(@NotNull T obj);

        @NotNull T deserialize(CompoundTag tag);
    }

    public static class LockableReadWriter<T>{
        public SyncLock readLock = new SyncLock();
        private final ReadWriter<T> readWriter;

        public LockableReadWriter(ReadWriter<T> readWriter){
            this.readWriter = readWriter;
        }

        public void readAndUpdateWithKey(CompoundTag tag){
            if(readLock.isLocked())return;
            readLock.update();
            readWriter.onReadWithKey(tag);
        }

        public void readAndUpdateDefault(CompoundTag tag){
            if(readLock.isLocked())return;
            readLock.update();
            readWriter.onReadDefault(tag);
        }

        public void writeDefault(CompoundTag tag){
            // if(readLock.isLocked())return;
            // readLock.update();
            readWriter.onWriteDefault(tag);
        }


    }

    public static class LockableReadWriteExecutor{
        public SyncLock readLock = new SyncLock();
        private final ReadWriteExecutor readWriteExecutor;

        public LockableReadWriteExecutor(ReadWriteExecutor readWriteExecutor){
            this.readWriteExecutor = readWriteExecutor;
        }

        public void readAndUpdateWithKey(CompoundTag tag){
            if(readLock.isLocked())return;
            readLock.update();
            readWriteExecutor.onReadWithKey(tag);
        }

        public void readAndUpdateDefault(CompoundTag tag){
            if(readLock.isLocked())return;
            readLock.update();
            readWriteExecutor.onReadDefault(tag);
        }

        public void writeWithKey(CompoundTag tag){
            // if(lock.isLocked())return;
            readWriteExecutor.onWriteWithKey(tag);
        }

        public void writeDefault(CompoundTag tag){
            // if(lock.isLocked())return;
            readWriteExecutor.onWriteDefault(tag);
        }
    }

    public static class ReadWriter<T> extends ReadWriteExecutor {
        private final Supplier<T> supplier;
        private final Consumer<T> consumer;
        private final Serializer<T> serializer;
        private final NetworkKey key;

        private ReadWriter(Supplier<T> supplier, Consumer<T> consumer, Serializer<T> serializer, NetworkKey key){
            super(
                    tag -> consumer.accept(serializer.deserialize(tag)),
                    tag -> tag.put(key.getSerializedName(), serializer.serialize(supplier.get())),
                    key
            );
            this.supplier = supplier;
            this.consumer = consumer;
            this.serializer = serializer;
            this.key = key;

        }

        public static <T> ReadWriter<T> of(Supplier<T> supplier, Consumer<T> consumer, Serializer<T> serializer, NetworkKey key){
            return new ReadWriter<>(
                    supplier, consumer, serializer, key
            );
        }


        public NetworkKey getKey() {
            return key;
        }

        public void onReadWithKey(CompoundTag tag){
            if(!tag.contains(key.getSerializedName()))return;
            consumer.accept(serializer.deserialize(tag.getCompound(key.getSerializedName())));
        }

        public void onReadDefault(CompoundTag tag){
            // if(!tag.contains(key))return;
            consumer.accept(serializer.deserialize(tag.getCompound(key.getSerializedName())));
        }
 
        public void onWriteDefault(CompoundTag tag){
            tag.put(key.getSerializedName(), serializer.serialize(supplier.get()));
        }


    }

    public static class ReadWriteExecutor{
        Consumer<CompoundTag> onRead;
        Consumer<CompoundTag> onWrite;
        NetworkKey key;


        public NetworkKey getKey() {
            return key;
        }

        protected ReadWriteExecutor(
                Consumer<CompoundTag> onRead,
                Consumer<CompoundTag> onWrite,
                NetworkKey key
        ) {
            this.onRead = onRead;
            this.onWrite = onWrite;
            this.key = key;
        }


        public static ReadWriteExecutor of(Consumer<CompoundTag> onRead, Consumer<CompoundTag> onWrite, NetworkKey key){
            return new ReadWriteExecutor(
                    onRead, onWrite, key
            );
        }

        public void onReadWithKey(CompoundTag t){
            if(!t.contains(key.getSerializedName()))return;
            onRead.accept(t.getCompound(key.getSerializedName()));
        }

        public void onReadDefault(CompoundTag t){
            onRead.accept(t);
        }

        public void onWriteDefault(CompoundTag t){
            onWrite.accept(t);
        }

        public void onWriteWithKey(CompoundTag t){
            CompoundTag tag = new CompoundTag();
            onWrite.accept(tag);
            t.put(key.getSerializedName(), tag);
        }

    }

}
