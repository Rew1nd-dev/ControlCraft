package com.verr1.vscontrolcraft.base.UltraTerminal;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class ExposedFieldRequestPacket extends SimplePacketBase {
    private final BlockPos pos;

    public ExposedFieldRequestPacket(BlockPos pos) {
        this.pos = pos;
    }

    public ExposedFieldRequestPacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(()->{
            BlockEntity be = context.getSender().level().getExistingBlockEntity(pos);
            if(be instanceof ITerminalDevice device){
                var availableFields = device
                        .fields()
                        .stream()
                        .map(e -> new ExposedFieldMessage(
                                    e.type,
                                    e.min_max.x,
                                    e.min_max.y,
                                    e.directionOptional
                                )
                        )
                        .toList();
                var currentField = device.getExposedField().type;
                AllPackets.sendToPlayer(
                        new ExposedFieldOpenScreenPacket(
                            availableFields,
                            currentField,
                            pos
                        ),
                        context.getSender()
                );
            }
        });
        return true;
    }
}
