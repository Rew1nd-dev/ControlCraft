package com.verr1.vscontrolcraft.deprecated;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.jetRudder.JetRudderBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class JetRudderSyncAnimationPacket extends SimplePacketBase {
    private final BlockPos pos;
    private final double horizontal;
    private final double vertical;
    private final double thrust;


    public JetRudderSyncAnimationPacket(BlockPos pos, double horizontal, double vertical, double thrust) {
        this.pos = pos;
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.thrust = thrust;
    }

    public JetRudderSyncAnimationPacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        horizontal = buffer.readDouble();
        vertical = buffer.readDouble();
        thrust = buffer.readDouble();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeDouble(horizontal);
        buffer.writeDouble(vertical);
        buffer.writeDouble(thrust);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity blockEntity = Minecraft.getInstance().player.level().getExistingBlockEntity(pos);
            if(blockEntity instanceof JetRudderBlockEntity jet){
                jet.setAnimatedAngles((float) horizontal, (float) vertical, (float) thrust);
            }
        });
        return true;
    }
}
