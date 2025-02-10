package com.verr1.vscontrolcraft.base.UltraTerminal;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class ExposedFieldOpenScreenPacket extends SimplePacketBase {
    private List<ExposedFieldMessage> availableFields = new ArrayList<>();
    private ExposedFieldType currentField;
    private BlockPos pos;
    private int size;

    public ExposedFieldOpenScreenPacket(List<ExposedFieldMessage> availableFields, ExposedFieldType currentField, BlockPos pos) {
        this.availableFields = availableFields;
        this.currentField = currentField;
        this.size = availableFields.size();
        this.pos = pos;
    }

    public ExposedFieldOpenScreenPacket(FriendlyByteBuf buffer) {
        size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            availableFields.add(
                    new ExposedFieldMessage(
                            buffer.readEnum(ExposedFieldType.class),
                            buffer.readDouble(),
                            buffer.readDouble()
                    )
            );
        }
        currentField = buffer.readEnum(ExposedFieldType.class);
        pos = buffer.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(size);
        for (ExposedFieldMessage field : availableFields) {
            buffer.writeEnum(field.type());
            buffer.writeDouble(field.min());
            buffer.writeDouble(field.max());
        }
        buffer.writeEnum(currentField);
        buffer.writeBlockPos(pos);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            // It Should Occur On The Client Side
            LocalPlayer player = Minecraft.getInstance().player;
            Level world = player.level();
            if (!world.isLoaded(pos))
                return;

            if(pos == null)return;
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ScreenOpener.open(new ExposedFieldSettingScreen(pos, availableFields, currentField)
            ));

        });
        return true;
    }
}
