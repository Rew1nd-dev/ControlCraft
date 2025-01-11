package com.verr1.vscontrolcraft.blocks.propeller;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class PropellerSettingsPacket extends SimplePacketBase {


    private BlockPos pos;
    private final boolean reverseTorque;
    private final double thrust_ratio;
    private final double torque_ratio;

    public PropellerSettingsPacket(BlockPos pos, boolean reverseTorque, double thrust_ratio, double torque_ratio) {
        this.pos = pos;
        this.reverseTorque = reverseTorque;
        this.thrust_ratio = thrust_ratio;
        this.torque_ratio = torque_ratio;
    }

    public PropellerSettingsPacket(FriendlyByteBuf buffer) {
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
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity be = context.getSender().level().getBlockEntity(pos);
            if(be instanceof PropellerBlockEntity){
                PropellerBlockEntity propeller = (PropellerBlockEntity) be;
                propeller.setProperty(torque_ratio, thrust_ratio, reverseTorque);
            }

        });
        return true;
    }
}
