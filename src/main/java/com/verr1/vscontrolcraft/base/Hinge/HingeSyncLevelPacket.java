package com.verr1.vscontrolcraft.base.Hinge;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class HingeSyncLevelPacket extends SimplePacketBase {
    private BlockPos hingePos;
    private HingeAdjustLevel level;

    public HingeSyncLevelPacket(BlockPos hingePos, HingeAdjustLevel level) {
        this.hingePos = hingePos;
        this.level = level;
    }

    public HingeSyncLevelPacket(FriendlyByteBuf buffer) {
        this.hingePos = buffer.readBlockPos();
        this.level = buffer.readEnum(HingeAdjustLevel.class);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(hingePos);
        buffer.writeEnum(level);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity blockEntity = Minecraft.getInstance().player.level().getExistingBlockEntity(hingePos);
            if(blockEntity instanceof IAdjustableHinge hinge){
                hinge.setAdjustment(level);
            }
        });
        return true;
    }
}
