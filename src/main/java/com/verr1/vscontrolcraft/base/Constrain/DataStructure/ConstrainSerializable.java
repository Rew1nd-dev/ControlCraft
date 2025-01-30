package com.verr1.vscontrolcraft.base.Constrain.DataStructure;

import com.verr1.vscontrolcraft.utils.VSConstrainSerializeUtils;
import net.minecraft.nbt.CompoundTag;
import org.valkyrienskies.core.apigame.constraints.*;

public record ConstrainSerializable(VSConstraint constraint) {

    public static ConstrainSerializable deserialize(CompoundTag tag) {
        String type = tag.getString("type");
        return switch (type.toLowerCase()) {
            case "attachment" ->
                    new ConstrainSerializable(VSConstrainSerializeUtils.readVSAttachmentConstrain(tag, "constrain"));
            case "hinge_orientation" ->
                    new ConstrainSerializable(VSConstrainSerializeUtils.readVSHingeOrientationConstrain(tag, "constrain"));
            case "slide" ->
                    new ConstrainSerializable(VSConstrainSerializeUtils.readVSSlideConstrain(tag, "constrain"));
            case "fixed_orientation" ->
                    new ConstrainSerializable(VSConstrainSerializeUtils.readVSFixedOrientationConstraint(tag, "constrain"));
            case "rope" ->
                    new ConstrainSerializable(VSConstrainSerializeUtils.readVSRopeConstraint(tag, "constrain"));
            case "pos_damping" ->
                    new ConstrainSerializable(VSConstrainSerializeUtils.readVSPosDampingConstraint(tag, "constrain"));
            default -> null;
        };
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", constraint.getConstraintType().toString());
        switch (constraint.getConstraintType()) {
            case ATTACHMENT -> VSConstrainSerializeUtils.writeVSAttachmentConstrain(tag, "constrain", (VSAttachmentConstraint) constraint);
            case HINGE_ORIENTATION -> VSConstrainSerializeUtils.writeVSHingeOrientationConstrain(tag, "constrain", (VSHingeOrientationConstraint) constraint);
            case SLIDE -> VSConstrainSerializeUtils.writeVSSlideConstrain(tag, "constrain", (VSSlideConstraint) constraint);
            case FIXED_ORIENTATION -> VSConstrainSerializeUtils.writeVSFixedOrientationConstraint(tag, "constrain", (VSFixedOrientationConstraint) constraint);
            case ROPE -> VSConstrainSerializeUtils.writeVSRopeConstraint(tag, "constrain", (VSRopeConstraint) constraint);
            case POS_DAMPING -> VSConstrainSerializeUtils.writeVSPosDampingConstraint(tag, "constrain", (VSPosDampingConstraint) constraint);
            default -> {
            }
        }
        return tag;
    }

}
