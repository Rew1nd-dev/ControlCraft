package com.verr1.controlcraft.foundation.network.packets.specific;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldMessage;
import com.verr1.controlcraft.foundation.type.descriptive.SlotDirection;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class ExposedFieldOpenScreenPacket extends SimplePacketBase {
    private List<ExposedFieldMessage> availableFields = new ArrayList<>();

    private final BlockPos pos;
    private final int size;

    public ExposedFieldOpenScreenPacket(List<ExposedFieldMessage> availableFields, BlockPos pos) {
        this.availableFields = availableFields;
        this.size = availableFields.size();
        this.pos = pos;
    }

    public ExposedFieldOpenScreenPacket(FriendlyByteBuf buffer) {
        size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            availableFields.add(
                    new ExposedFieldMessage(
                            buffer.readEnum(SlotType.class),
                            buffer.readDouble(),
                            buffer.readDouble(),
                            buffer.readEnum(SlotDirection.class)
                    )
            );
        }

        pos = buffer.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(size);
        for (ExposedFieldMessage field : availableFields) {
            buffer.writeEnum(field.type());
            buffer.writeDouble(field.min());
            buffer.writeDouble(field.max());
            buffer.writeEnum(field.openTo());
        }

        buffer.writeBlockPos(pos);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {

        });
        return true;
    }
}
