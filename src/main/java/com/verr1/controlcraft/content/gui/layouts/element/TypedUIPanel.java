package com.verr1.controlcraft.content.gui.layouts.element;

import com.verr1.controlcraft.content.gui.layouts.NetworkUIPort;
import com.verr1.controlcraft.foundation.api.delegate.IRemoteDevice;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import net.minecraft.core.BlockPos;

import static com.verr1.controlcraft.content.gui.factory.GenericUIFactory.boundBlockEntity;

public abstract class TypedUIPanel <T> extends NetworkUIPort<T> {

    private final BlockPos boundPos;
    private final NetworkKey key;

    public TypedUIPanel(BlockPos boundPos, NetworkKey key, Class<T> dataType, T defaultValue) {
        super($ -> {}, () -> defaultValue);
        this.boundPos = boundPos;
        this.key = key;
    }


    protected void trigger(){
        T input = readGUI();
        boundBlockEntity(boundPos, IRemoteDevice.class).map(IRemoteDevice::panel).ifPresent(be -> {
            if(input != null) {
                be.request(input, boundPos, key);
            }
        });
    }

    @Override
    protected abstract T readGUI();

    @Override
    protected final void writeGUI(T value) {
        // does nothing
    }
}
