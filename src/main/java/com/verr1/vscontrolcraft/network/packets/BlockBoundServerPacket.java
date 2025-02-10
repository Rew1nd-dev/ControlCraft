package com.verr1.vscontrolcraft.network.packets;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialAnchorBlockEntity;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockBoundServerPacket extends BlockBoundPacket {

    private BlockBoundServerPacket(
            BlockPos boundPos,
            BlockBoundPacketType type,
            List<Double> doubles,
            List<Long> longs,
            List<String> utf8s,
            List<Boolean> booleans) {
        super(boundPos, type, doubles, longs, utf8s, booleans);

    }

    public BlockBoundServerPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            BlockEntity be = context.getSender().level().getBlockEntity(getBoundPos());
            if(be instanceof IPacketHandler handler){
                handler.handleServer(context, this);
            }

        });
        return true;
    }

    public static class builder{

        private final BlockPos boundPos;
        private final BlockBoundPacketType type;
        private final List<Double> doubles = new ArrayList<>();
        private final List<Long> longs = new ArrayList<>();
        private final List<String> utf8s = new ArrayList<>();
        private final List<Boolean> booleans = new ArrayList<>();


        public builder(BlockPos pos, BlockBoundPacketType type){
            this.boundPos = pos;
            this.type = type;
        }

        public builder withDouble(double d){
            this.doubles.add(d);
            return this;
        }

        public builder withLong(long l){
            this.longs.add(l);
            return this;
        }

        public builder withUtf8(String s){
            this.utf8s.add(s);
            return this;
        }

        public builder withBoolean(boolean b){
            this.booleans.add(b);
            return this;
        }

        public BlockBoundServerPacket build(){
            return new BlockBoundServerPacket(boundPos, type, doubles, longs, utf8s, booleans);
        }


    }

}
