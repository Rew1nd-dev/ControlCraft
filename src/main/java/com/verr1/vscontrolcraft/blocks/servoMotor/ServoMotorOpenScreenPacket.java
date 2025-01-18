package com.verr1.vscontrolcraft.blocks.servoMotor;

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

public class ServoMotorOpenScreenPacket extends SimplePacketBase {
    private double p;
    private double d;
    private double i;

    private double angle;
    private BlockPos pos;

    public ServoMotorOpenScreenPacket(ServoMotorBlockEntity.pid params, double a, BlockPos pos) {
        this.p = params.p();
        this.d = params.d();
        this.i = params.i();
        this.angle = a;
        this.pos = pos;
    }

    public ServoMotorOpenScreenPacket(FriendlyByteBuf buf) {
        p = buf.readDouble();
        d = buf.readDouble();
        i = buf.readDouble();
        angle = buf.readDouble();
        pos = buf.readBlockPos();
    }


    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeDouble(p);
        buffer.writeDouble(d);
        buffer.writeDouble(i);
        buffer.writeDouble(angle);
        buffer.writeBlockPos(pos);
    }

    @OnlyIn(value = Dist.CLIENT)
    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            // It Should Occur On The Client Side
            LocalPlayer player = Minecraft.getInstance().player;
            Level world = player.level();
            if (world == null || !world.isLoaded(pos))
                return;

            if(pos == null)return;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ScreenOpener.open(new ServoMotorScreen(pos, p, i, d, angle));
            });

        });
        return true;
    }
}
