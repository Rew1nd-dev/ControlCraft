package com.verr1.vscontrolcraft.base.Constrain.DataStructure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public record ConstrainKey(BlockPos pos, String dimension, String name, boolean ship_1_isGround, boolean ship_2_isGround, boolean temp) {
    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ConstrainKey key))return false;

        // only pos, dimension and name matters

        return  key.pos.equals(pos) &&
                key.dimension.equals(dimension) &&
                key.name.equals(name);
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("pos", pos.asLong());
        tag.putString("name", name);
        tag.putString("level", dimension);
        tag.putBoolean("ship_1_isGround", ship_1_isGround);
        tag.putBoolean("ship_2_isGround", ship_2_isGround);
        tag.putBoolean("temp", temp);
        return tag;
    }

    public static ConstrainKey deserialize(CompoundTag tag) {
        var pos = BlockPos.of(tag.getLong("pos"));
        var level = tag.getString("level");
        var name = tag.getString("name");
        var ship_1_isGround = tag.getBoolean("ship_1_isGround");
        var ship_2_isGround = tag.getBoolean("ship_2_isGround");
        var temp = tag.getBoolean("temp");
        return new ConstrainKey(pos, level, name, ship_1_isGround, ship_2_isGround, temp);
    }


}
