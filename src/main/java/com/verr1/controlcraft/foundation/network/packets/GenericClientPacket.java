package com.verr1.controlcraft.foundation.network.packets;

import com.verr1.controlcraft.foundation.network.handler.ClientGenericPacketHandler;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class GenericClientPacket extends BlockBoundPacket{


    protected GenericClientPacket(BlockPos boundPos, RegisteredPacketType type, List<Double> doubles, List<Long> longs, List<String> utf8s, List<Boolean> booleans) {
        super(boundPos, type, doubles, longs, utf8s, booleans);

    }

    public GenericClientPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> ClientGenericPacketHandler.dispatchPacket(this, context));
        return true;
    }


    public static class builder{

        private final BlockPos boundPos;
        private final RegisteredPacketType type;
        private List<Double> doubles = new ArrayList<>();
        private List<Long> longs = new ArrayList<>();
        private List<String> utf8s = new ArrayList<>();
        private List<Boolean> booleans = new ArrayList<>();


        public builder(RegisteredPacketType type){
            this.boundPos = BlockPos.ZERO;
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

        public GenericClientPacket build(){
            return new GenericClientPacket(boundPos, type, doubles, longs, utf8s, booleans);
        }


    }

}
