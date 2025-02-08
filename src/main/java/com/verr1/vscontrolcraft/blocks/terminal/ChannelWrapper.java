package com.verr1.vscontrolcraft.blocks.terminal;

import com.verr1.vscontrolcraft.registry.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class ChannelWrapper implements ItemLike {
    private BlockPos pos;
    private int rows;
    private String title;
    private final ArrayList<TerminalRowData> row_data = new ArrayList<>();

    public CompoundTag getInventoryTag() {
        return inventoryTag;
    }

    private final CompoundTag inventoryTag = new CompoundTag();

    public void overrideData(List<TerminalBlockEntity.TerminalChannel> channels, BlockPos pos, String title){
        this.pos = pos;
        this.title = title;
        this.rows = channels.size();
        row_data.clear();
        channels.stream().map(e -> new TerminalRowData(
                e.isListening(),
                e.getField().name(),
                e.getField().value(),
                e.getMinMax()
        )).forEach(row_data::add);
    }


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

    public ChannelWrapper(){

    }

    public ChannelWrapper(FriendlyByteBuf buf){
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


    public void serialize(CompoundTag invNbt){
        inventoryTag.put("items", invNbt);
    }


    public BlockPos getPos() {
        return pos;
    }

    public int getRows() {
        return rows;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<TerminalRowData> getRow_data() {
        return row_data;
    }

    @Override
    public @NotNull Item asItem() {
        return AllBlocks.TERMINAL_BLOCK.asItem();
    }
}
