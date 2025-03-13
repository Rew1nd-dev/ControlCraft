package com.verr1.controlcraft.utils;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.apigame.joints.*;

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

    public static Serializer<VSJointMaxForceTorque> MAX_FORCE_TORQUE = of(
            max -> {
                CompoundTag tag = new CompoundTag();
                tag.put("max_force", FLOAT.serializeNullable(max.getMaxForce()));
                tag.put("max_torque", FLOAT.serializeNullable(max.getMaxTorque()));
                return tag;
            },
            tag -> {
                Float force = FLOAT.deserializeNullable(tag.getCompound("max_force"));
                Float torque = FLOAT.deserializeNullable(tag.getCompound("max_torque"));
                if(force == null || torque == null){
                    return null;
                }
                return new VSJointMaxForceTorque(force, torque);
            }
    );

    public static Serializer<VSD6Joint.AngularLimitPair> ANGULAR_LIMIT_PAIR = of(
            pair -> {
                CompoundTag tag = new CompoundTag();
                tag.put("min", FLOAT.serialize(pair.getLowerLimit()));
                tag.put("max", FLOAT.serialize(pair.getUpperLimit()));
                tag.put("restitution", FLOAT.serializeNullable(pair.getRestitution()));
                tag.put("bounce_threshold", FLOAT.serializeNullable(pair.getBounceThreshold()));
                tag.put("stiffness", FLOAT.serializeNullable(pair.getStiffness()));
                tag.put("damping", FLOAT.serializeNullable(pair.getDamping()));
                return tag;
            },
            tag -> new VSD6Joint.AngularLimitPair(
                    FLOAT.deserialize(tag.getCompound("min")),
                    FLOAT.deserialize(tag.getCompound("max")),
                    FLOAT.deserializeNullable(tag.getCompound("restitution")),
                    FLOAT.deserializeNullable(tag.getCompound("bounce_threshold")),
                    FLOAT.deserializeNullable(tag.getCompound("stiffness")),
                    FLOAT.deserializeNullable(tag.getCompound("damping"))
            )
    );

    public static Serializer<VSD6Joint.LinearLimitPair> LINEAR_LIMIT_PAIR = of(
            pair -> {
                CompoundTag tag = new CompoundTag();
                tag.put("min", FLOAT.serialize(pair.getLowerLimit()));
                tag.put("max", FLOAT.serialize(pair.getUpperLimit()));
                tag.put("restitution", FLOAT.serializeNullable(pair.getRestitution()));
                tag.put("bounce_threshold", FLOAT.serializeNullable(pair.getBounceThreshold()));
                tag.put("stiffness", FLOAT.serializeNullable(pair.getStiffness()));
                tag.put("damping", FLOAT.serializeNullable(pair.getDamping()));
                return tag;
            },
            tag -> new VSD6Joint.LinearLimitPair(
                    FLOAT.deserialize(tag.getCompound("min")),
                    FLOAT.deserialize(tag.getCompound("max")),
                    FLOAT.deserializeNullable(tag.getCompound("restitution")),
                    FLOAT.deserializeNullable(tag.getCompound("bounce_threshold")),
                    FLOAT.deserializeNullable(tag.getCompound("stiffness")),
                    FLOAT.deserializeNullable(tag.getCompound("damping"))
            )
    );

    public static Serializer<VSRevoluteJoint.VSRevoluteDriveVelocity> REVOLUTE_DRIVE_VELOCITY = of(
            drive -> {
                CompoundTag tag = new CompoundTag();
                tag.put("velocity", FLOAT.serialize(drive.getVelocity()));
                tag.put("auto_wake", BOOLEAN.serialize(drive.getAutoWake()));
                return tag;
            },
            tag -> new VSRevoluteJoint.VSRevoluteDriveVelocity(
                    FLOAT.deserialize(tag.getCompound("velocity")),
                    BOOLEAN.deserialize(tag.getCompound("auto_wake"))
            )
    );

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
                tag.put("position", VECTOR3D.serialize(pose.getPos()));
                tag.put("rotation", QUATERNION4D.serialize(pose.getRot()));
                return tag;
            },
            tag -> new VSJointPose(
                    VECTOR3D.deserialize(tag.getCompound("position")),
                    QUATERNION4D.deserialize(tag.getCompound("rotation"))
            )
    );

    public static Serializer<VSJoint> JOINT = of(
            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("ship_id_0", LONG.serialize(Optional.ofNullable(joint.getShipId0()).orElse(-1L)));
                tag.put("ship_id_1", LONG.serialize(Optional.ofNullable(joint.getShipId1()).orElse(-1L)));
                tag.put("pose_0", JOINT_POSE.serialize(joint.getPose0()));
                tag.put("pose_1", JOINT_POSE.serialize(joint.getPose1()));

                return tag;
            },
            tag -> null
    );

    public static Serializer<VSFixedJoint> FIXED_JOINT = of(
            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("joint", JOINT.serialize(joint));
                tag.put("max_force_torque", MAX_FORCE_TORQUE.serializeNullable(joint.getMaxForceTorque()));
                return tag;
            },
            tag -> {
                CompoundTag commonJoint = tag.getCompound("joint");
                return new VSFixedJoint(
                        LONG.deserialize(commonJoint.getCompound("ship_id_0")),
                        JOINT_POSE.deserialize(commonJoint.getCompound("joint").getCompound("pose_0")),
                        LONG.deserialize(commonJoint.getCompound("joint").getCompound("ship_id_1")),
                        JOINT_POSE.deserialize(commonJoint.getCompound("joint").getCompound("pose_1")),
                        MAX_FORCE_TORQUE.deserializeNullable(tag.getCompound("max_force_torque"))
                );
            }
    );

    public static Serializer<VSPrismaticJoint> PRISMATIC_JOINT = of(
            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("joint", JOINT.serialize(joint));
                tag.put("max_force_torque", MAX_FORCE_TORQUE.serializeNullable(joint.getMaxForceTorque()));
                tag.put("linear_limit_pair", LINEAR_LIMIT_PAIR.serializeNullable(joint.getLinearLimitPair()));
                return tag;
            },
            tag -> {
                CompoundTag commonJoint = tag.getCompound("joint");
                return new VSPrismaticJoint(
                        LONG.deserialize(commonJoint.getCompound("ship_id_0")),
                        JOINT_POSE.deserialize(commonJoint.getCompound("pose_0")),
                        LONG.deserialize(commonJoint.getCompound("ship_id_1")),
                        JOINT_POSE.deserialize(commonJoint.getCompound("pose_1")),
                        MAX_FORCE_TORQUE.deserializeNullable(tag.getCompound("max_force_torque")),
                        LINEAR_LIMIT_PAIR.deserializeNullable(tag.getCompound("linear_limit_pair"))
                );
            }
    );

    public static Serializer<VSDistanceJoint> DISTANCE_JOINT = of(
            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("joint", JOINT.serialize(joint));
                tag.put("max_force_torque", MAX_FORCE_TORQUE.serializeNullable(joint.getMaxForceTorque()));
                tag.put("min", FLOAT.serializeNullable(joint.getMinDistance()));
                tag.put("max", FLOAT.serializeNullable(joint.getMinDistance()));
                tag.put("tolerance", FLOAT.serializeNullable(joint.getTolerance()));
                tag.put("stiffness", FLOAT.serializeNullable(joint.getStiffness()));
                tag.put("damping", FLOAT.serializeNullable(joint.getDamping()));
                return tag;
            },
            tag -> {
                CompoundTag commonJoint = tag.getCompound("joint");
                return new VSDistanceJoint(
                        LONG.deserialize(commonJoint.getCompound("ship_id_0")),
                        JOINT_POSE.deserialize(commonJoint.getCompound("pose_0")),
                        LONG.deserialize(commonJoint.getCompound("ship_id_1")),
                        JOINT_POSE.deserialize(commonJoint.getCompound("pose_1")),
                        MAX_FORCE_TORQUE.deserializeNullable(tag.getCompound("max_force_torque")),
                        FLOAT.deserialize(tag.getCompound("min")),
                        FLOAT.deserialize(tag.getCompound("max")),
                        FLOAT.deserializeNullable(tag.getCompound("tolerance")),
                        FLOAT.deserializeNullable(tag.getCompound("stiffness")),
                        FLOAT.deserializeNullable(tag.getCompound("damping"))
                );
            }
    );

    public static Serializer<VSRevoluteJoint> REVOLUTE_JOINT = of(
            joint -> {
                CompoundTag tag = new CompoundTag();
                tag.put("joint", JOINT.serialize(joint));
                tag.put("max_force_torque", MAX_FORCE_TORQUE.serializeNullable(joint.getMaxForceTorque()));
                tag.put("angular_limit_pair", ANGULAR_LIMIT_PAIR.serializeNullable(joint.getAngularLimitPair()));
                tag.put("drive_velocity", REVOLUTE_DRIVE_VELOCITY.serializeNullable(joint.getDriveVelocity()));
                tag.put("drive_force_limit", FLOAT.serializeNullable(joint.getDriveForceLimit()));
                tag.put("drive_gear_ratio", FLOAT.serializeNullable(joint.getDriveGearRatio()));
                tag.put("drive_free_spin", BOOLEAN.serializeNullable(joint.getDriveFreeSpin()));
                return tag;
            },
            tag -> {
                CompoundTag commonJoint = tag.getCompound("joint");
                return new VSRevoluteJoint(
                        LONG.deserialize(commonJoint.getCompound("ship_id_0")),
                        JOINT_POSE.deserialize(commonJoint.getCompound("pose_0")),
                        LONG.deserialize(commonJoint.getCompound("ship_id_1")),
                        JOINT_POSE.deserialize(commonJoint.getCompound("pose_1")),
                        MAX_FORCE_TORQUE.deserializeNullable(tag.getCompound("max_force_torque")),
                        ANGULAR_LIMIT_PAIR.deserializeNullable(tag.getCompound("angular_limit_pair")),
                        REVOLUTE_DRIVE_VELOCITY.deserializeNullable(tag.getCompound("drive_velocity")),
                        FLOAT.deserializeNullable(tag.getCompound("drive_force_limit")),
                        FLOAT.deserializeNullable(tag.getCompound("drive_gear_ratio")),
                        BOOLEAN.deserializeNullable(tag.getCompound("drive_free_spin"))
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

    public static class ReadWriter<T>{
        Supplier<T> supplier;
        Consumer<T> consumer;
        Serializer<T> serializer;
        String key;

        public static <T> ReadWriter<T> of(Supplier<T> supplier, Consumer<T> consumer, Serializer<T> serializer, String key){
            ReadWriter<T> rw = new ReadWriter<>();
            rw.supplier = supplier;
            rw.consumer = consumer;
            rw.serializer = serializer;
            rw.key = key;
            return rw;
        }

        public void onRead(CompoundTag tag){
            consumer.accept(serializer.deserialize(tag.getCompound(key)));
        }
 
        public void onWrite(CompoundTag tag){
            tag.put(key, serializer.serialize(supplier.get()));
        }

    }

    public static class ReadWriteExecutor{
        Consumer<CompoundTag> onRead;
        Consumer<CompoundTag> onWrite;

        public static ReadWriteExecutor of(Consumer<CompoundTag> onRead, Consumer<CompoundTag> onWrite){
            ReadWriteExecutor rw = new ReadWriteExecutor();
            rw.onRead = onRead;
            rw.onWrite = onWrite;
            return rw;
        }

        public void onRead(CompoundTag t){
            onRead.accept(t);
        }

        public void onWrite(CompoundTag t){
            onWrite.accept(t);
        }
    }

}
