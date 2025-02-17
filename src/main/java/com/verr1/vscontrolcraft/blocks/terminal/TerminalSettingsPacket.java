package com.verr1.vscontrolcraft.blocks.terminal;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialAnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class TerminalSettingsPacket extends SimplePacketBase {

    private final BlockPos pos;
    private final int rows;
    private final int exposedChannelIndex;
    private final List<Vector2d> row_min_max = new ArrayList<>();
    private final List<Boolean> row_enabled = new ArrayList<>();
    private final List<Boolean> row_reversed = new ArrayList<>();

    public TerminalSettingsPacket(List<TerminalRowSetting> rowSettings, BlockPos pos, int exposedChannelIndex) {
        rowSettings.stream().map(TerminalRowSetting::enabled).forEach(row_enabled::add);
        rowSettings.stream().map(TerminalRowSetting::min_max).forEach(row_min_max::add);
        rowSettings.stream().map(TerminalRowSetting::isReversed).forEach(row_reversed::add);
        this.rows = rowSettings.size();
        this.pos = pos;
        this.exposedChannelIndex = exposedChannelIndex;
    }

    public TerminalSettingsPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        rows = buf.readInt();
        exposedChannelIndex = buf.readInt();
        for (int i = 0; i < rows; i++) {
            row_enabled.add(buf.readBoolean());
            row_min_max.add(new Vector2d(buf.readDouble(), buf.readDouble()));
            row_reversed.add(buf.readBoolean());
        }
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(rows);
        buffer.writeInt(exposedChannelIndex);
        for (int i = 0; i < rows; i++) {
            buffer.writeBoolean(row_enabled.get(i));
            buffer.writeDouble(row_min_max.get(i).x);
            buffer.writeDouble(row_min_max.get(i).y);
            buffer.writeBoolean(row_reversed.get(i));
        }
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity be = context.getSender().level().getBlockEntity(pos);
            if(be instanceof TerminalBlockEntity terminal){
                terminal.setMinMax(row_min_max);
                terminal.setEnabled(row_enabled);
                terminal.setReversed(row_reversed);
                terminal.setExposedChannel(exposedChannelIndex);
                DeferralExecutor.executeLater(terminal::setFrequency, 10);
            }
        });
        return true;
    }
}
