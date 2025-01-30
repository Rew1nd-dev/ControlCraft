package com.verr1.vscontrolcraft.blocks.anchor;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class AnchorOpenScreenPacket extends SimplePacketBase {
    private double airResistance;
    private double extraGravity;
    private BlockPos pos;

    public AnchorOpenScreenPacket(double airResistance, double extraGravity, BlockPos pos) {
        this.airResistance = airResistance;
        this.pos = pos;
        this.extraGravity = extraGravity;
    }

    public AnchorOpenScreenPacket(FriendlyByteBuf buf){
        airResistance = buf.readDouble();
        pos = buf.readBlockPos();
        extraGravity = buf.readDouble();
    }


    @Override
    public void write(FriendlyByteBuf buffer) {

        buffer.writeDouble(airResistance);
        buffer.writeBlockPos(pos);
        buffer.writeDouble(extraGravity);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            Level world = player.level();
            if (world == null || !world.isLoaded(pos))
                return;

            if(pos == null)return;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ScreenOpener.open(new AnchorScreen(pos, airResistance, extraGravity));
            });
        });
        return true;
    }
}
