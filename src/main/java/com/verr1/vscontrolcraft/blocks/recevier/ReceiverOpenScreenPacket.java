package com.verr1.vscontrolcraft.blocks.recevier;

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

public class ReceiverOpenScreenPacket extends SimplePacketBase {
    private BlockPos pos;
    private String name;
    private String peripheralType;
    private long protocol;

    public ReceiverOpenScreenPacket(BlockPos pos, String name, String peripheralType, long protocol) {
        this.pos = pos;
        this.name = name;
        this.peripheralType = peripheralType;
        this.protocol = protocol;
    }

    public ReceiverOpenScreenPacket(FriendlyByteBuf buffer) {

        name = buffer.readUtf();
        peripheralType = buffer.readUtf();
        pos = buffer.readBlockPos();
        protocol = buffer.readLong();

    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(name);
        buffer.writeUtf(peripheralType);
        buffer.writeBlockPos(pos);
        buffer.writeLong(protocol);
    }

    public int maxRange(){return 20;}

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
                ScreenOpener.open(new ReceiverScreen(pos, name, peripheralType, protocol));
            });

        });
        return true;
    }
}
