package com.verr1.controlcraft.foundation.redstone;

import com.verr1.controlcraft.foundation.data.NetworkKey;
import net.minecraft.nbt.CompoundTag;

public interface IReceiver {

    NetworkKey FIELD_ = NetworkKey.create("field_");

    DirectReceiver receiver();

    String name();


}
