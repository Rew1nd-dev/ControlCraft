package com.verr1.vscontrolcraft.deprecated;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialScreen;
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
    private final boolean isRunning;
    private final boolean isStatic;

    public SpatialOpenScreenPacket(BlockPos pos, double offset, long protocol, boolean isStatic, boolean isRunning) {
        this.pos = pos;
        this.offset = offset;
        this.protocol = protocol;
        this.isRunning = isRunning;
        this.isStatic = isStatic;
    }

    public SpatialOpenScreenPacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        offset = buffer.readDouble();
        protocol = buffer.readLong();
        isRunning = buffer.readBoolean();
        isStatic = buffer.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeDouble(offset);
        buffer.writeLong(protocol);
        buffer.writeBoolean(isRunning);
        buffer.writeBoolean(isStatic);
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
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ScreenOpener.open(new SpatialScreen(pos, offset, protocol, isRunning, isStatic)));
        });
        return true;
    }
}
