package com.verr1.vscontrolcraft.blocks.jetRudder;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class JetRudderSyncAnimationPacket extends SimplePacketBase {
    private final BlockPos pos;
    private final double horizontal;
    private final double vertical;

    public JetRudderSyncAnimationPacket(BlockPos pos, double horizontal, double vertical) {
        this.pos = pos;
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    public JetRudderSyncAnimationPacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        horizontal = buffer.readDouble();
        vertical = buffer.readDouble();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeDouble(horizontal);
        buffer.writeDouble(vertical);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity blockEntity = Minecraft.getInstance().player.level().getExistingBlockEntity(pos);
            if(blockEntity instanceof JetRudderBlockEntity jet){
                jet.setAnimatedAngles((float) horizontal, (float) vertical);
            }
        });
        return true;
    }
}
