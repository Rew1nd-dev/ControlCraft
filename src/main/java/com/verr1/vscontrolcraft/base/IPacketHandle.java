package com.verr1.vscontrolcraft.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IPacketHandle<T extends BlockEntity> {
    void readBuffer(FriendlyByteBuf buffer);

    void writeBuffer(FriendlyByteBuf buffer);

    void handle(T be);
}
