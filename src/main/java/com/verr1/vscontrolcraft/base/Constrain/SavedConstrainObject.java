package com.verr1.vscontrolcraft.base.Constrain;

import com.verr1.vscontrolcraft.base.Constrain.DataStructure.ConstrainKey;
import com.verr1.vscontrolcraft.base.Constrain.DataStructure.ConstrainSerializable;
import net.minecraft.nbt.CompoundTag;

public record SavedConstrainObject(ConstrainKey key, ConstrainSerializable constrain) {

    public static SavedConstrainObject deserialize(CompoundTag tag) {
        return new SavedConstrainObject(
                ConstrainKey.deserialize(tag.getCompound("key")),
                ConstrainSerializable.deserialize(tag.getCompound("constrain"))
        );
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.put("key", key.serialize());
        tag.put("constrain", constrain.serialize());
        return tag;
    }
}
