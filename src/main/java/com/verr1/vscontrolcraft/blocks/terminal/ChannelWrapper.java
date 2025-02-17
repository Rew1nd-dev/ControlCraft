package com.verr1.vscontrolcraft.blocks.terminal;

import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldType;
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
    private int exposedIndex;
    private final ArrayList<TerminalRowData> row_data = new ArrayList<>();

    public CompoundTag getInventoryTag() {
        return inventoryTag;
    }

    private final CompoundTag inventoryTag = new CompoundTag();

    public void overrideData(List<TerminalBlockEntity.TerminalChannel> channels, BlockPos pos, String title, int exposedIndex){
        this.pos = pos;
        this.title = title;
        this.rows = channels.size();
        this.exposedIndex = exposedIndex;
        row_data.clear();
        channels.stream().map(e -> new TerminalRowData(
                e.isListening(),
                e.getField().type,
                e.getField().field.value(),
                e.getMinMax(),
                e.isBoolean(),
                e.isReversed()
        )).forEach(row_data::add);
    }


    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(rows);
        buffer.writeUtf(title);
        buffer.writeInt(exposedIndex);
        for(int i = 0; i < rows; i++){
            buffer.writeBoolean(row_data.get(i).enabled());
            buffer.writeEnum(row_data.get(i).type());

            buffer.writeDouble(row_data.get(i).value());
            buffer.writeDouble(row_data.get(i).min_max().x());
            buffer.writeDouble(row_data.get(i).min_max().y());

            buffer.writeBoolean(row_data.get(i).isBoolean());
            buffer.writeBoolean(row_data.get(i).isReverse());
        }
    }

    public ChannelWrapper(){

    }

    public ChannelWrapper(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
        rows = buf.readInt();
        title = buf.readUtf();
        exposedIndex = buf.readInt();
        for(int i = 0; i < rows; i++){
            var rowData = new TerminalRowData(
                    buf.readBoolean(),
                    buf.readEnum(ExposedFieldType.class),
                    buf.readDouble(),
                    new Vector2d(buf.readDouble(), buf.readDouble()),
                    buf.readBoolean(),
                    buf.readBoolean()
            );
            row_data.add(rowData);
        }
    }


    public void serialize(CompoundTag invNbt){
        inventoryTag.put("items", invNbt);
    }

    public CompoundTag saveToTag(){
        CompoundTag tag = new CompoundTag();
        tag.put("inv", inventoryTag.getCompound("items"));
        return tag;
    }

    public void loadFromTag(CompoundTag tag){
        serialize(tag.getCompound("inv"));
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

    public int getExposedIndex(){
        return exposedIndex;
    }

    public ArrayList<TerminalRowData> getRow_data() {
        return row_data;
    }

    @Override
    public @NotNull Item asItem() {
        return AllBlocks.TERMINAL_BLOCK.asItem();
    }
}
