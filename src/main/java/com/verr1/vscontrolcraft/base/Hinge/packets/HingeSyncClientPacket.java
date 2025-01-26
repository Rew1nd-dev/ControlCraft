package com.verr1.vscontrolcraft.base.Hinge.packets;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.base.Hinge.HingeAdjustLevel;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IAdjustableHinge;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IFlippableHinge;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class HingeSyncClientPacket extends SimplePacketBase {
    private final BlockPos hingePos;
    private final HingeAdjustLevel level;
    private final Boolean isFlipped;

    public HingeSyncClientPacket(BlockPos hingePos, HingeAdjustLevel level, Boolean isFlipped) {
        this.hingePos = hingePos;
        this.level = level;
        this.isFlipped = isFlipped;
    }

    public HingeSyncClientPacket(FriendlyByteBuf buffer) {
        this.hingePos = buffer.readBlockPos();
        this.level = buffer.readEnum(HingeAdjustLevel.class);
        this.isFlipped = buffer.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(hingePos);
        buffer.writeEnum(level);
        buffer.writeBoolean(isFlipped);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity blockEntity = Minecraft.getInstance().player.level().getExistingBlockEntity(hingePos);
            if(blockEntity instanceof IAdjustableHinge hinge){
                hinge.setAdjustment(level);
            }
            if(blockEntity instanceof IFlippableHinge hinge){
                hinge.setFlipped(isFlipped);
            }
        });
        return true;
    }
}
