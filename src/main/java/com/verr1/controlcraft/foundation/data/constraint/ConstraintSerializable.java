package com.verr1.controlcraft.foundation.data.constraint;

import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.nbt.CompoundTag;
import org.valkyrienskies.core.apigame.constraints.*;

public record ConstraintSerializable(VSConstraint constraint) {

    public static ConstraintSerializable deserialize(CompoundTag tag) {
        String type = tag.getString("type");
        VSConstraint joint =
        switch (VSConstraintType.valueOf(type)) {
            case ATTACHMENT -> SerializeUtils.ATTACH.deserialize(tag.getCompound("nbt"));
            case FIXED_ORIENTATION -> SerializeUtils.FIXED_ORIENT.deserialize(tag.getCompound("nbt"));
            case HINGE_ORIENTATION -> SerializeUtils.ORIENT.deserialize(tag.getCompound("nbt"));
            case SLIDE -> SerializeUtils.SLIDE.deserialize(tag.getCompound("nbt"));
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
        return new ConstraintSerializable(joint);
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", constraint.getConstraintType().name());
        CompoundTag constraintTag =
        switch (constraint.getConstraintType()) {
            case ATTACHMENT -> SerializeUtils.ATTACH.serialize((VSAttachmentConstraint) constraint);
            case FIXED_ORIENTATION -> SerializeUtils.FIXED_ORIENT.serialize((VSFixedOrientationConstraint) constraint);
            case HINGE_ORIENTATION -> SerializeUtils.ORIENT.serialize((VSHingeOrientationConstraint) constraint);
            case SLIDE -> SerializeUtils.SLIDE.serialize((VSSlideConstraint) constraint);
            default -> throw new IllegalStateException("Unexpected value: " + constraint.getConstraintType().name());
        };
        tag.put("nbt", constraintTag);
        return tag;
    }

}
