package com.verr1.controlcraft.foundation.network.packets.specific;


import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.data.terminal.TerminalRowSetting;
import com.verr1.controlcraft.content.blocks.terminal.TerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TerminalSettingsPacket extends SimplePacketBase {

    private final BlockPos pos;
    private final int size;
    private final List<TerminalRowSetting> data = new ArrayList<>();

    public TerminalSettingsPacket(List<TerminalRowSetting> rowSettings, BlockPos pos) {
        data.addAll(rowSettings);
        this.size = rowSettings.size();
        this.pos = pos;

    }

    public TerminalSettingsPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        size = buf.readInt();
        for (int i = 0; i < size; i++) {
            boolean enabled = buf.readBoolean();
            Couple<Double> minMax = Couple.create(buf.readDouble(), buf.readDouble());
            boolean isReversed = buf.readBoolean();
            data.add(new TerminalRowSetting(minMax, enabled, isReversed));
        }
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(size);
        for (int i = 0; i < size; i++) {
            buffer.writeBoolean(data.get(i).enabled());
            buffer.writeDouble(data.get(i).min_max().get(true));
            buffer.writeDouble(data.get(i).min_max().get(false));
            buffer.writeBoolean(data.get(i).isReversed());
        }
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() ->
                Optional
                    .ofNullable(context.getSender())
                    .map(ServerPlayer::serverLevel)
                    .flatMap(level -> BlockEntityGetter.getLevelBlockEntityAt(level, pos, TerminalBlockEntity.class))
                    .ifPresent(terminal -> {
                                terminal.setMinMax(data.stream().map(TerminalRowSetting::min_max).toList());
                                terminal.setEnabled(data.stream().map(TerminalRowSetting::enabled).toList());
                                terminal.setReversed(data.stream().map(TerminalRowSetting::isReversed).toList());
                                ControlCraftServer.SERVER_EXECUTOR.executeLater(terminal::setFrequency, 10);
                        }
            ));
        return true;
    }
}
