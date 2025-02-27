package com.verr1.vscontrolcraft.base.Servo;

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

public class PIDControllerOpenScreenPacket extends SimplePacketBase {
    private double p;
    private double d;
    private double i;

    private PIDControllerType type;
    private double value;
    private double target;
    private BlockPos pos;

    public PIDControllerOpenScreenPacket(PID params, double value, double target, BlockPos pos, PIDControllerType type) {
        this.p = params.p();
        this.d = params.d();
        this.i = params.i();
        this.value = value;
        this.target = target;
        this.pos = pos;
        this.type = type;
    }

    public PIDControllerOpenScreenPacket(FriendlyByteBuf buf) {
        p = buf.readDouble();
        d = buf.readDouble();
        i = buf.readDouble();
        value = buf.readDouble();
        target = buf.readDouble();
        pos = buf.readBlockPos();
        type = buf.readEnum(PIDControllerType.class);
    }


    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeDouble(p);
        buffer.writeDouble(d);
        buffer.writeDouble(i);
        buffer.writeDouble(value);
        buffer.writeDouble(target);
        buffer.writeBlockPos(pos);
        buffer.writeEnum(type);
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
                ScreenOpener.open(new PIDControllerScreen(pos, p, i, d, value, target));
            });

        });
        return true;
    }
}
