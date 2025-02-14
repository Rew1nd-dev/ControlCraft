package com.verr1.vscontrolcraft.base.UltraTerminal;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class ExposedFieldResetPacket extends SimplePacketBase {
    private final BlockPos pos;

    public ExposedFieldResetPacket(BlockPos pos) {
        this.pos = pos;
    }

    public ExposedFieldResetPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(()->{
            BlockEntity be = context.getSender().level().getExistingBlockEntity(pos);
            if(be instanceof ITerminalDevice device){
                device.reset();
            }
        });
        return true;
    }
}
