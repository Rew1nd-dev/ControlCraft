package com.verr1.controlcraft.content.compact.tweak;

import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.WorldAttached;
import com.verr1.controlcraft.ControlCraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;

public class TweakedLinkedControllerServerHandlerExtension {

    public static WorldAttached<Map<UUID, Collection<TweakedManualFrequency>>> receivedInputs = new WorldAttached<>(($) -> {
        return new HashMap<>();
    });

    public static WorldAttached<Map<UUID, ArrayList<TweakedManualAxisFrequency>>> receivedAxes = new WorldAttached<>(($) -> {
        return new HashMap<>();
    });

    public static class TweakedManualFrequency extends IntAttached<Couple<RedstoneLinkNetworkHandler.Frequency>> implements $IRedstoneLinkable {
        private BlockPos pos;

        public TweakedManualFrequency(BlockPos pos, Couple<RedstoneLinkNetworkHandler.Frequency> second) {
            super(30, second);
            this.pos = pos;
        }

        public void updatePosition(BlockPos pos) {
            this.pos = pos;
            this.setFirst(30);
        }

        public int getTransmittedStrength() {
            return this.isAlive() ? 15 : 0;
        }

        public boolean isAlive() {
            return this.getFirst() > 0;
        }

        public BlockPos getLocation() {
            return this.pos;
        }

        public void setReceivedStrength(int power) {
        }

        public boolean isListening() {
            return false;
        }

        public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
            return this.getSecond();
        }

        @Override
        public void $setReceivedStrength(int power) {

        }
    }


    public static class TweakedManualAxisFrequency extends IntAttached<Couple<RedstoneLinkNetworkHandler.Frequency>> implements $IRedstoneLinkable {
        private BlockPos pos;
        private float level = 0;

        public TweakedManualAxisFrequency(BlockPos pos, float level, Couple<RedstoneLinkNetworkHandler.Frequency> second) {
            super(30, second);
            this.pos = pos;
            this.level = level;
        }

        public void updatePosition(BlockPos pos) {
            this.pos = pos;
            this.setFirst(30);
        }

        public void SetLevel(float level) {
            this.level = level;
        }

        public int getTransmittedStrength() {
            return this.isAlive() ? decimal2Level(level) : 0;
        }

        private static int decimal2Level(float fullPrecision){
            float d256 = fullPrecision * 256;
            return (int)((d256 - (int)d256) * 15);
        }

        public boolean isAlive() {
            return this.getFirst() > 0;
        }

        public BlockPos getLocation() {
            return this.pos;
        }

        public void setReceivedStrength(int power) {
        }

        public boolean isListening() {
            return false;
        }

        public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
            return this.getSecond();
        }

        @Override
        public void $setReceivedStrength(int power) {

        }
    }


    public static void tick(LevelAccessor world) {
        Map<UUID, Collection<TweakedManualFrequency>> map = receivedInputs.get(world);
        Iterator<Map.Entry<UUID, Collection<TweakedManualFrequency>>> iterator = map.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<UUID, Collection<TweakedManualFrequency>> entry = iterator.next();
            Collection<TweakedManualFrequency> list = entry.getValue();
            Iterator<TweakedManualFrequency> entryIterator = list.iterator();

            while(entryIterator.hasNext()) {
                TweakedManualFrequency TweakedManualFrequency = entryIterator.next();
                TweakedManualFrequency.decrement();
                if (!TweakedManualFrequency.isAlive()) {
                    ControlCraftServer.DECIMAL_LINK_NETWORK_HANDLER.removeFromNetwork(world, TweakedManualFrequency);
                    entryIterator.remove();
                }
            }

            if (list.isEmpty()) {
                iterator.remove();
            }
        }

        Map<UUID, ArrayList<TweakedManualAxisFrequency>> map2 = receivedAxes.get(world);
        Iterator<Map.Entry<UUID, ArrayList<TweakedManualAxisFrequency>>> iterator2 = map2.entrySet().iterator();

        while(iterator2.hasNext()) {
            Map.Entry<UUID, ArrayList<TweakedManualAxisFrequency>> entry = iterator2.next();
            ArrayList<TweakedManualAxisFrequency> list = entry.getValue();
            Iterator<TweakedManualAxisFrequency> entryIterator = list.iterator();

            while(entryIterator.hasNext()) {
                TweakedManualAxisFrequency TweakedManualAxisFrequency = entryIterator.next();
                TweakedManualAxisFrequency.decrement();
                if (!TweakedManualAxisFrequency.isAlive()) {
                    ControlCraftServer.DECIMAL_LINK_NETWORK_HANDLER.removeFromNetwork(world, TweakedManualAxisFrequency);
                    entryIterator.remove();
                } else {
                    ControlCraftServer.DECIMAL_LINK_NETWORK_HANDLER.updateNetworkOf(world, TweakedManualAxisFrequency);
                }
            }

            if (list.isEmpty()) {
                iterator2.remove();
            }
        }

    }


    public static void ReceiveAxis(LevelAccessor world, BlockPos pos, UUID uniqueID, ArrayList<Couple<RedstoneLinkNetworkHandler.Frequency>> collect, ArrayList<Float> values) {
        Map<UUID, ArrayList<TweakedManualAxisFrequency>> map = receivedAxes.get(world);
        ArrayList<TweakedManualAxisFrequency> list = map.computeIfAbsent(uniqueID, $ -> new ArrayList<>(10));

        label23:
        for(int i = 0; i < collect.size(); ++i) {
            Iterator<TweakedManualAxisFrequency> iterator = list.iterator();

            TweakedManualAxisFrequency entry;
            do {
                if (!iterator.hasNext()) {
                    TweakedManualAxisFrequency entry1 = new TweakedManualAxisFrequency(pos, values.get(i), collect.get(i));
                    ControlCraftServer.DECIMAL_LINK_NETWORK_HANDLER.addToNetwork(world, entry1);
                    list.add(entry1);
                    continue label23;
                }

                entry = iterator.next();
            } while(!(entry.getSecond()).equals(collect.get(i)));

            entry.SetLevel(values.get(i));
            entry.updatePosition(pos);
        }

    }


}
