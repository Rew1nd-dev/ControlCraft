package com.verr1.vscontrolcraft.deprecated;

import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import com.verr1.vscontrolcraft.blocks.recevier.PeripheralKey;
import com.verr1.vscontrolcraft.blocks.recevier.ReceiverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class ReceiverRegisterPacket extends BlockEntityConfigurationPacket<ReceiverBlockEntity> {
    private String name;
    private long protocol;

    public ReceiverRegisterPacket(BlockPos pos, String name, long protocol) {
        super(pos);
        this.name = name;
        this.protocol = protocol;
    }

    public ReceiverRegisterPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    protected void writeSettings(FriendlyByteBuf buffer) {
        buffer.writeUtf(name);
        buffer.writeLong(protocol);
    }

    @Override
    protected void readSettings(FriendlyByteBuf buffer) {
        name = buffer.readUtf();
        protocol = buffer.readLong();
    }

    @Override
    protected void applySettings(ReceiverBlockEntity blockEntity) {
        blockEntity.resetNetworkRegistry(new PeripheralKey(name, protocol));

    }

}
