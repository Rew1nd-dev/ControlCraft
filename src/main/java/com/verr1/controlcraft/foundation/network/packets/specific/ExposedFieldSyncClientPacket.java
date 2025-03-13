package com.verr1.controlcraft.foundation.network.packets.specific;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.foundation.api.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldMessage;
import com.verr1.controlcraft.foundation.type.ExposedFieldDirection;
import com.verr1.controlcraft.foundation.type.ExposedFieldType;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class ExposedFieldSyncClientPacket extends SimplePacketBase {

    private List<ExposedFieldMessage> messages = new ArrayList<>();

    private final BlockPos pos;
    private final int size;

    public static void syncClient(ITerminalDevice device, BlockPos pos, Level world){
        var availableFields =
                device.fields()
                        .stream()
                        .map(e -> new ExposedFieldMessage(
                                        e.type,
                                        e.min_max.get(true),
                                        e.min_max.get(false),
                                        e.directionOptional
                                )
                        )
                        .toList();
        var p = new ExposedFieldSyncClientPacket(availableFields, pos);
        ControlCraftPackets.sendToNear(world, pos, 16, p);
    }

    public ExposedFieldSyncClientPacket(List<ExposedFieldMessage> messages, BlockPos pos) {
        this.messages = messages;
        this.size = messages.size();
        this.pos = pos;
    }

    public ExposedFieldSyncClientPacket(FriendlyByteBuf buffer) {
        size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            messages.add(
                    new ExposedFieldMessage(
                            buffer.readEnum(ExposedFieldType.class),
                            buffer.readDouble(),
                            buffer.readDouble(),
                            buffer.readEnum(ExposedFieldDirection.class)
                    )
            );
        }

        pos = buffer.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(size);
        for (ExposedFieldMessage field : messages) {
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
            // It Should Occur On The Client Side
            LocalPlayer player = Minecraft.getInstance().player;
            if(player == null)return;
            Level world = player.level();
            if (!world.isLoaded(pos)) return;

            BlockEntity be = world.getExistingBlockEntity(pos);
            if(!(be instanceof ITerminalDevice device))return;
            for(int i =0; i <  min(device.fields().size(), messages.size()); i++){
                device.fields().get(i).directionOptional = messages.get(i).openTo();
                device.fields().get(i).min_max = Couple.create(messages.get(i).min(), messages.get(i).max());
            }

        });
        return true;
    }
}
