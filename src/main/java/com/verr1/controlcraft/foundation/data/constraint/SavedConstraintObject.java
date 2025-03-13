package com.verr1.controlcraft.foundation.data.constraint;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.apigame.joints.VSJoint;

import java.util.Optional;

public record SavedConstraintObject(@NotNull ConstraintKey key, @Nullable ConstraintSerializable constrain) {

    public @Nullable VSJoint getConstraint(){
        return Optional.ofNullable(constrain()).map(ConstraintSerializable::constraint).orElse(null);
    }

    public static SavedConstraintObject deserialize(CompoundTag tag) {
        return new SavedConstraintObject(
                ConstraintKey.deserialize(tag.getCompound("key")),
                ConstraintSerializable.deserialize(tag.getCompound("constrain"))
        );
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("key", key.serialize());
        tag.put("constrain",
                Optional
                .ofNullable(constrain())
                .map(ConstraintSerializable::serialize)
                .orElse(new CompoundTag())
        );
        return tag;
    }
}
