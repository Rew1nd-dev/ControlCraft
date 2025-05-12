package com.verr1.controlcraft.content.gui.layouts.element;

import com.verr1.controlcraft.content.blocks.NetworkBlockEntity;
import com.verr1.controlcraft.content.gui.layouts.NetworkUIPort;
import com.verr1.controlcraft.foundation.api.delegate.INetworkHandle;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import net.minecraft.core.BlockPos;

import static com.verr1.controlcraft.content.gui.factory.GenericUIFactory.boundBlockEntity;

public abstract class TypedUIPort<T> extends NetworkUIPort<T> {


    public TypedUIPort(BlockPos boundPos, NetworkKey key, Class<T> dataType, T defaultValue) {
        super(
                d -> boundBlockEntity(boundPos, INetworkHandle.class).ifPresent(be -> be.handler().writeClientBuffer(key, d, dataType)),
                () -> boundBlockEntity(boundPos, INetworkHandle.class)
                        .map(be -> be.handler().readClientBuffer(key, dataType))
                        .orElse(defaultValue)
        );
    }

}
