package com.verr1.vscontrolcraft.network.packets;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class BlockBoundPacket extends SimplePacketBase {
    public BlockPos getBoundPos() {
        return boundPos;
    }

    private final BlockPos boundPos;



    private final BlockBoundPacketType type;
    private final int doubleLength;
    private List<Double> doubles = new ArrayList<>();
    private final int longLength;
    private List<Long> longs = new ArrayList<>();
    private final int utf8Length;
    private List<String> utf8s = new ArrayList<>();
    private final int booleanLength;
    private List<Boolean> booleans = new ArrayList<>();

    protected BlockBoundPacket(
            BlockPos boundPos,
            BlockBoundPacketType type,
            List<Double> doubles,
            List<Long> longs,
            List<String> utf8s,
            List<Boolean> booleans
    ) {
        this.boundPos = boundPos;
        this.type = type;
        this.doubles = doubles;
        this.longs = longs;
        this.utf8s = utf8s;
        this.booleans = booleans;
        this.doubleLength = doubles.size();
        this.longLength = longs.size();
        this.utf8Length = utf8s.size();
        this.booleanLength = booleans.size();
    }

    public BlockBoundPacket(FriendlyByteBuf buf){
        boundPos = buf.readBlockPos();
        type = buf.readEnum(BlockBoundPacketType.class);
        doubleLength = buf.readInt();
        for (int i = 0; i < doubleLength; i++) {
            doubles.add(buf.readDouble());
        }
        longLength = buf.readInt();
        for (int i = 0; i < longLength; i++) {
            longs.add(buf.readLong());
        }
        utf8Length = buf.readInt();
        for (int i = 0; i < utf8Length; i++) {
            utf8s.add(buf.readUtf());
        }
        booleanLength = buf.readInt();
        for (int i = 0; i < booleanLength; i++) {
            booleans.add(buf.readBoolean());
        }
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(boundPos);
        buffer.writeEnum(type);
        buffer.writeInt(doubleLength);
        for (Double d : doubles) {
            buffer.writeDouble(d);
        }
        buffer.writeInt(longLength);
        for (Long l : longs) {
            buffer.writeLong(l);
        }
        buffer.writeInt(utf8Length);
        for (String s : utf8s) {
            buffer.writeUtf(s);
        }
        buffer.writeInt(booleanLength);
        for (Boolean b : booleans) {
            buffer.writeBoolean(b);
        }
    }




    @Override
    public abstract boolean handle(NetworkEvent.Context context);


    public List<Double> getDoubles() {
        return doubles;
    }

    public List<Long> getLongs() {
        return longs;
    }

    public List<String> getUtf8s() {
        return utf8s;
    }

    public BlockBoundPacketType getType() {
        return type;
    }

    public List<Boolean> getBooleans() {
        return booleans;
    }
}
