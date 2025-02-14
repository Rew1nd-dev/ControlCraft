package com.verr1.vscontrolcraft.base.UltraTerminal;

import com.simibubi.create.foundation.networking.SimplePacketBase;
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

public class ExposedFieldSyncClientPacket extends SimplePacketBase {
    private List<ExposedFieldMessage> fieldSettings = new ArrayList<>();

    private final BlockPos pos;
    private final int size;

    public ExposedFieldSyncClientPacket(List<ExposedFieldMessage> fieldSettings, BlockPos pos) {
        this.fieldSettings = fieldSettings;
        this.size = fieldSettings.size();
        this.pos = pos;
    }

    public ExposedFieldSyncClientPacket(FriendlyByteBuf buffer) {
        size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            fieldSettings.add(
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
        for (ExposedFieldMessage field : fieldSettings) {
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
            Level world = player.level();
            if (!world.isLoaded(pos)) return;

            BlockEntity be = world.getExistingBlockEntity(pos);
            if(be instanceof ITerminalDevice device){
                device.handleClient(fieldSettings);
            }

        });
        return true;
    }
}
