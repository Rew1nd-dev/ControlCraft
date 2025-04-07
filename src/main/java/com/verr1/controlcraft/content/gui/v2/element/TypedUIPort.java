package com.verr1.controlcraft.content.gui.v2.element;

import com.verr1.controlcraft.content.blocks.NetworkBlockEntity;
import com.verr1.controlcraft.content.gui.v1.layouts.NetworkUIPort;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;

import static com.verr1.controlcraft.content.gui.v1.factory.GenericUIFactory.boundBlockEntity;

public abstract class TypedUIPort<T> extends NetworkUIPort<T> {


    public TypedUIPort(BlockPos boundPos, NetworkKey key, Class<T> dataType, T defaultValue) {
        super(
                d -> boundBlockEntity(boundPos, NetworkBlockEntity.class).ifPresent(be -> be.writeClientBuffer(key, d, dataType)),
                () -> boundBlockEntity(boundPos, NetworkBlockEntity.class)
                        .map(be -> be.readClientBuffer(key, dataType))
                        .orElse(defaultValue)
        );
    }

}
