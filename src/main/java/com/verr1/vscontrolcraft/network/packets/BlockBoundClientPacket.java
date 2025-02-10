package com.verr1.vscontrolcraft.network.packets;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialScreen;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockBoundClientPacket extends BlockBoundPacket {


    private BlockBoundClientPacket(
            BlockPos boundPos,
            BlockBoundPacketType type,
            List<Double> doubles,
            List<Long> longs,
            List<String> utf8s,
            List<Boolean> booleans) {
        super(boundPos, type, doubles, longs, utf8s, booleans);

    }

    public BlockBoundClientPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            // It Should Occur On The Client Side
            LocalPlayer player = Minecraft.getInstance().player;
            Level world = player.level();
            if (world == null || !world.isLoaded(getBoundPos()))
                return;

            if(world.getExistingBlockEntity(getBoundPos()) instanceof IPacketHandler handler){
                handler.handleClient(context, this);
            }

        });
        return true;
    }

    public static class builder{

        private final BlockPos boundPos;
        private final BlockBoundPacketType type;
        private List<Double> doubles = new ArrayList<>();
        private List<Long> longs = new ArrayList<>();
        private List<String> utf8s = new ArrayList<>();
        private List<Boolean> booleans = new ArrayList<>();


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

        public BlockBoundClientPacket build(){
            return new BlockBoundClientPacket(boundPos, type, doubles, longs, utf8s, booleans);
        }


    }

}


