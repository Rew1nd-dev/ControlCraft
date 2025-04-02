package com.verr1.controlcraft.utils;

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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SerializeUtils {
    public static Serializer<Double> DOUBLE = of(SerializeUtils::ofDouble, tag -> tag.getDouble("value"));
    public static Serializer<Float> FLOAT = of(SerializeUtils::ofFloat, tag -> tag.getFloat("value"));
    public static Serializer<Integer> INT = of(SerializeUtils::ofInt, tag -> tag.getInt("value"));
    public static Serializer<Long> LONG = of(SerializeUtils::ofLong, tag -> tag.getLong("value"));
    public static Serializer<Boolean> BOOLEAN = of(SerializeUtils::ofBoolean, tag -> tag.getBoolean("value"));
    public static Serializer<String> STRING = of(SerializeUtils::ofString, tag -> tag.getString("value"));


    public static Serializer<Vector3dc> VECTOR3D = of(
            vec -> {
                CompoundTag tag = new CompoundTag();
                tag.putDouble("x", vec.x());
                tag.putDouble("y", vec.y());
                tag.putDouble("z", vec.z());
                return tag;
            },
            tag -> new Vector3d(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"))
    );
    public static Serializer<Quaterniondc> QUATERNION4D = of(
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

    public static Serializer<VSJointPose> JOINT_POSE = of(
            pose -> {
                CompoundTag tag = new CompoundTag();
                tag.put("p", VECTOR3D.serialize(pose.getPos()));
                tag.put("q", QUATERNION4D.serialize(pose.getRot()));
                return tag;
            },
            tag -> new VSJointPose(
                    VECTOR3D.deserialize(tag.getCompound("p")),
                    QUATERNION4D.deserialize(tag.getCompound("q")))
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
                tag.put("p_0", VECTOR3D.serialize(joint.getLocalPos0()));
                tag.put("p_1", VECTOR3D.serialize(joint.getLocalPos1()));
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
                        VECTOR3D.deserialize(tag.getCompound("p_0")),
                        VECTOR3D.deserialize(tag.getCompound("p_1")),
                        DOUBLE.deserialize(tag.getCompound("m_f")),
                        DOUBLE.deserialize(tag.getCompound("d"))
                );
            }
    );

    public static Serializer<VSHingeOrientationConstraint> ORIENT = of(

            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("joint", CONSTRAINT.serialize(joint));
                tag.put("q_0", QUATERNION4D.serialize(joint.getLocalRot0()));
                tag.put("q_1", QUATERNION4D.serialize(joint.getLocalRot1()));
                tag.put("m_t", DOUBLE.serialize(joint.getMaxTorque()));
                return tag;
            },
            tag -> {
                CompoundTag commonJoint = tag.getCompound("joint");
                return new VSHingeOrientationConstraint(
                        LONG.deserialize(commonJoint.getCompound("ship_id_0")),
                        LONG.deserialize(commonJoint.getCompound("ship_id_1")),
                        DOUBLE.deserialize(commonJoint.getCompound("compliance")),
                        QUATERNION4D.deserialize(tag.getCompound("q_0")),
                        QUATERNION4D.deserialize(tag.getCompound("q_1")),
                        DOUBLE.deserialize(tag.getCompound("m_t"))
                );
            }
    );

    public static Serializer<VSFixedOrientationConstraint> FIXED_ORIENT = of(
            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("joint", CONSTRAINT.serialize(joint));
                tag.put("q_0", QUATERNION4D.serialize(joint.getLocalRot0()));
                tag.put("q_1", QUATERNION4D.serialize(joint.getLocalRot1()));
                tag.put("m_t", DOUBLE.serialize(joint.getMaxTorque()));
                return tag;
            },
            tag -> {
                CompoundTag commonJoint = tag.getCompound("joint");
                return new VSFixedOrientationConstraint(
                        LONG.deserialize(commonJoint.getCompound("ship_id_0")),
                        LONG.deserialize(commonJoint.getCompound("ship_id_1")),
                        DOUBLE.deserialize(commonJoint.getCompound("compliance")),
                        QUATERNION4D.deserialize(tag.getCompound("q_0")),
                        QUATERNION4D.deserialize(tag.getCompound("q_1")),
                        DOUBLE.deserialize(tag.getCompound("m_t"))
                );
            }
    );

    public static Serializer<VSSlideConstraint> SLIDE = of(
            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("joint", CONSTRAINT.serialize(joint));
                tag.put("p_0", VECTOR3D.serialize(joint.getLocalPos0()));
                tag.put("p_1", VECTOR3D.serialize(joint.getLocalPos1()));
                tag.put("m_f", DOUBLE.serialize(joint.getMaxForce()));
                tag.put("axis", VECTOR3D.serialize(joint.getLocalSlideAxis0()));
                tag.put("m_d", DOUBLE.serialize(joint.getMaxDistBetweenPoints()));
                return tag;
            },
            tag -> {
                CompoundTag commonJoint = tag.getCompound("joint");
                return new VSSlideConstraint(
                        LONG.deserialize(commonJoint.getCompound("ship_id_0")),
                        LONG.deserialize(commonJoint.getCompound("ship_id_1")),
                        DOUBLE.deserialize(commonJoint.getCompound("compliance")),
                        VECTOR3D.deserialize(tag.getCompound("p_0")),
                        VECTOR3D.deserialize(tag.getCompound("p_1")),
                        DOUBLE.deserialize(tag.getCompound("m_f")),
                        VECTOR3D.deserialize(tag.getCompound("axis")),
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


    public static class ReadWriter<T>{
        private Supplier<T> supplier;
        private Consumer<T> consumer;
        private Serializer<T> serializer;
        private NetworkKey key;

        public static <T> ReadWriter<T> of(Supplier<T> supplier, Consumer<T> consumer, Serializer<T> serializer, NetworkKey key){
            ReadWriter<T> rw = new ReadWriter<>();
            rw.supplier = supplier;
            rw.consumer = consumer;
            rw.serializer = serializer;
            rw.key = key;
            return rw;
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


        public static ReadWriteExecutor of(Consumer<CompoundTag> onRead, Consumer<CompoundTag> onWrite, NetworkKey key){
            ReadWriteExecutor rw = new ReadWriteExecutor();
            rw.onRead = onRead;
            rw.onWrite = onWrite;
            rw.key = key;
            return rw;
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
