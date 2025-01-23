package com.verr1.vscontrolcraft.blocks.magnet;

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

public class MagnetOpenScreenPacket extends SimplePacketBase {
    private double strength;
    private BlockPos pos;

    public MagnetOpenScreenPacket(BlockPos pos, double strength) {
        this.pos = pos;
        this.strength = strength;
    }

    public MagnetOpenScreenPacket(FriendlyByteBuf buffer) {
        this.strength = buffer.readDouble();
        this.pos = buffer.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeDouble(strength);
        buffer.writeBlockPos(pos);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            // It Should Occur On The Client Side
            LocalPlayer player = Minecraft.getInstance().player;
            Level world = player.level();
            if (!world.isLoaded(pos))
                return;

            if(pos == null)return;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ScreenOpener.open(new MagnetScreen(pos, strength));
            });

        });
        return true;
    }
}
