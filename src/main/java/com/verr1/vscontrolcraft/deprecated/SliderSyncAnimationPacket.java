package com.verr1.vscontrolcraft.deprecated;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.slider.SliderControllerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class SliderSyncAnimationPacket extends SimplePacketBase {
    private final float distance;
    private final BlockPos pos;

    public SliderSyncAnimationPacket(BlockPos pos, float distance) {
        this.pos = pos;
        this.distance = distance;
    }

    public SliderSyncAnimationPacket(FriendlyByteBuf buf){
        this.pos = buf.readBlockPos();
        this.distance = buf.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeFloat(distance);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity blockEntity = Minecraft.getInstance().player.level().getExistingBlockEntity(pos);
            if(blockEntity instanceof SliderControllerBlockEntity slider){
                slider.setAnimatedDistance(distance);
            }
        });
        return true;
    }
}
