package com.verr1.vscontrolcraft.blocks.terminal;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.base.UltraTerminal.NumericField;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class TerminalOpenScreenPacket extends SimplePacketBase {
    private final BlockPos pos;
    private final int rows;

    private final String title;

    private final List<TerminalRowData> row_data = new ArrayList<>();



    public TerminalOpenScreenPacket(BlockPos pos, List<TerminalBlockEntity.TerminalChannel> channels, String title){
        this.title = title;
        this.rows = channels.size();
        this.pos = pos;
        channels.stream().map(e -> new TerminalRowData(
                e.isAlive(),
                e.getField().name(),
                e.getField().value(),
                e.getMinMax()
        )).forEach(row_data::add);


    }

    public TerminalOpenScreenPacket(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
        rows = buf.readInt();
        title = buf.readUtf();
        for(int i = 0; i < rows; i++){
            var rowData = new TerminalRowData(
                    buf.readBoolean(),
                    buf.readUtf(),
                    buf.readDouble(),
                    new Vector2d(buf.readDouble(), buf.readDouble())
            );
            row_data.add(rowData);
        }
    }


    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(rows);
        buffer.writeUtf(title);
        for(int i = 0; i < rows; i++){
            buffer.writeBoolean(row_data.get(i).enabled());
            buffer.writeUtf(row_data.get(i).name());

            buffer.writeDouble(row_data.get(i).value());
            buffer.writeDouble(row_data.get(i).min_max().x());
            buffer.writeDouble(row_data.get(i).min_max().y());
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            // It Should Occur On The Client Side
            LocalPlayer player = Minecraft.getInstance().player;
            Level world = player.level();
            if (world == null || !world.isLoaded(pos))
                return;

            if(pos == null)return;
            // DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ScreenOpener.open(new TerminalScreen(pos, title, row_data)));
        });
        return true;
    }
}
