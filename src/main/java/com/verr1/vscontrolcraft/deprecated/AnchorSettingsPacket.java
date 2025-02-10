package com.verr1.vscontrolcraft.deprecated;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.anchor.AnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class AnchorSettingsPacket extends SimplePacketBase {
    private double airResistance;
    private double extraGravity;
    private BlockPos pos;

    public AnchorSettingsPacket(double airResistance, double extraGravity, BlockPos pos) {
        this.airResistance = airResistance;
        this.pos = pos;
        this.extraGravity = extraGravity;
    }

    public AnchorSettingsPacket(FriendlyByteBuf buf){
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
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity be = context.getSender().level().getBlockEntity(pos);
            if(be instanceof AnchorBlockEntity anchor){
                anchor.setAirResistance(airResistance);
                anchor.setExtraGravity(extraGravity);
            }

        });
        return true;
    }
}
