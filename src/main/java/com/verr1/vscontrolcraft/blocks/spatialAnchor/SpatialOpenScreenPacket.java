package com.verr1.vscontrolcraft.blocks.spatialAnchor;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.recevier.ReceiverScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SpatialOpenScreenPacket extends SimplePacketBase {
    private final BlockPos pos;
    private final double offset;
    private final long protocol;

    public SpatialOpenScreenPacket(BlockPos pos, double offset, long protocol) {
        this.pos = pos;
        this.offset = offset;
        this.protocol = protocol;
    }

    public SpatialOpenScreenPacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        offset = buffer.readDouble();
        protocol = buffer.readLong();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeDouble(offset);
        buffer.writeLong(protocol);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            // It Should Occur On The Client Side
            LocalPlayer player = Minecraft.getInstance().player;
            Level world = player.level();
            if (world == null || !world.isLoaded(pos))
                return;

            if(pos == null)return;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ScreenOpener.open(new SpatialScreen(pos, offset, protocol)));
        });
        return true;
    }
}
