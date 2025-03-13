package com.verr1.controlcraft.foundation.data.constraint;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public record ConstraintKey(BlockPos pos, String dimension, String name){
    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ConstraintKey key))return false;

        // only pos, dimension and type matters

        return  key.pos.equals(pos) &&
                key.dimension.equals(dimension) &&
                key.name.equals(name);
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("pos", pos.asLong());
        tag.putString("type", name);
        tag.putString("level", dimension);
        return tag;
    }

    public static ConstraintKey deserialize(CompoundTag tag) {
        var pos = BlockPos.of(tag.getLong("pos"));
        var level = tag.getString("level");
        var name = tag.getString("type");
        return new ConstraintKey(pos, level, name);
    }


}
