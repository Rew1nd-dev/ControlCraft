package com.verr1.vscontrolcraft.blocks.propeller;

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

public class PropellerOpenScreenPacket extends SimplePacketBase {
    private BlockPos pos;
    private boolean reverseTorque;
    private double thrust_ratio;
    private double torque_ratio;

    public PropellerOpenScreenPacket(BlockPos pos, boolean reverseTorque, double thrust_ratio, double torque_ratio) {
        this.pos = pos;
        this.reverseTorque = reverseTorque;
        this.thrust_ratio = thrust_ratio;
        this.torque_ratio = torque_ratio;
    }

    public PropellerOpenScreenPacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        reverseTorque = buffer.readBoolean();
        thrust_ratio = buffer.readDouble();
        torque_ratio = buffer.readDouble();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(reverseTorque);
        buffer.writeDouble(thrust_ratio);
        buffer.writeDouble(torque_ratio);
    }


    @Override
    @OnlyIn(value = Dist.CLIENT)
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            // It Should Occur On The Client Side
            LocalPlayer player = Minecraft.getInstance().player;
            Level world = player.level();
            if (world == null || !world.isLoaded(pos))
                return;

            if(pos == null)return;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ScreenOpener.open(new PropellerScreen(pos, thrust_ratio, torque_ratio));
            });

        });
        return true;
    }
}
