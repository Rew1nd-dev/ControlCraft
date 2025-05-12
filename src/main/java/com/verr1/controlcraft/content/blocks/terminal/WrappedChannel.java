package com.verr1.controlcraft.content.blocks.terminal;

import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.foundation.data.terminal.TerminalRowData;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WrappedChannel implements ItemLike {

    private BlockPos pos;
    private int size = 0;



    private int validSize = 0;
    private final ArrayList<TerminalRowData> data = new ArrayList<>();



    private final CompoundTag inventoryTag = new CompoundTag();

    public WrappedChannel() {

    }

    public void overrideData(List<TerminalBlockEntity.TerminalChannel> channels, BlockPos pos, int validSize){
        this.pos = pos;
        this.size = channels.size();
        this.validSize = validSize;
        data.clear();
        channels.stream().map(e -> new TerminalRowData(
                e.isListening(),
                e.getType(),
                e.latestValue(),
                e.getMinMax(),
                e.isBoolean(),
                e.isReversed()
        )).forEach(data::add);
    }


    public void write(FriendlyByteBuf buffer){
        buffer.writeBlockPos(pos);
        buffer.writeInt(size);
        buffer.writeInt(validSize);
        for(int i = 0; i < size; i++){
            buffer.writeBoolean(data.get(i).enabled());
            buffer.writeEnum(data.get(i).type());

            buffer.writeDouble(data.get(i).value());
            buffer.writeDouble(data.get(i).min_max().get(true));
            buffer.writeDouble(data.get(i).min_max().get(false));
            buffer.writeBoolean(data.get(i).isBoolean());
            buffer.writeBoolean(data.get(i).isReversed());
        }
    }

    public CompoundTag inventoryTag() {
        return inventoryTag;
    }

    public WrappedChannel(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
        size = buf.readInt();
        validSize = buf.readInt();
        for(int i = 0; i < size; i++){
            boolean enabled = buf.readBoolean();
            SlotType type = buf.readEnum(SlotType.class);
            double value = buf.readDouble();
            double min = buf.readDouble();
            double max = buf.readDouble();
            boolean isBoolean = buf.readBoolean();
            boolean isReversed = buf.readBoolean();

            data.add(new TerminalRowData(enabled, type, value, Couple.create(min, max), isBoolean, isReversed));
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

    public int size(){return size;}

    public int validSize() {
        return validSize;
    }

    public ArrayList<TerminalRowData> data() {
        return data;
    }

    @Override
    public @NotNull Item asItem() {
        return ControlCraftBlocks.TERMINAL_BLOCK.asItem();
    }

}
