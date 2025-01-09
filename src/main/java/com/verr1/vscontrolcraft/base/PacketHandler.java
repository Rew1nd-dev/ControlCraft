package com.verr1.vscontrolcraft.base;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class PacketHandler {

    public PacketHandler() {

    }

    protected abstract void readBuffer(FriendlyByteBuf buffer);

    protected abstract void writeBuffer(FriendlyByteBuf buffer);

    protected abstract void handle(BlockEntity be);

}
