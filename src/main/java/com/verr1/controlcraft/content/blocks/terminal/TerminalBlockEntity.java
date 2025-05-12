package com.verr1.controlcraft.content.blocks.terminal;

import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.content.compact.tweak.$IRedstoneLinkable;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.network.executors.CompoundTagPort;
import com.verr1.controlcraft.foundation.redstone.IReceiver;
import com.verr1.controlcraft.foundation.redstone.RemoteReceiver;
import com.verr1.controlcraft.foundation.redstone.TerminalMenu;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import com.verr1.controlcraft.registry.ControlCraftMenuTypes;
import com.verr1.controlcraft.utils.CompoundTagBuilder;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.simibubi.create.Create.REDSTONE_LINK_NETWORK_HANDLER;
import static com.verr1.controlcraft.ControlCraftServer.DECIMAL_LINK_NETWORK_HANDLER;
import static java.lang.Math.min;

public class TerminalBlockEntity extends OnShipBlockEntity implements
        MenuProvider
{

    public static final Couple<RedstoneLinkNetworkHandler.Frequency> EMPTY_FREQUENCY = Couple.create(
            RedstoneLinkNetworkHandler.Frequency.EMPTY,
            RedstoneLinkNetworkHandler.Frequency.EMPTY
    );
    public static final int MAX_CHANNEL_SIZE = 16;

    public static final NetworkKey RECEIVER = NetworkKey.create("receiver");
    public static final NetworkKey CHANNEL = NetworkKey.create("channel");
    public static final NetworkKey WRAPPER = NetworkKey.create("wrapper");

    private final RemoteReceiver remoteReceiver = new RemoteReceiver();

    private final ArrayList<TerminalChannel> channels = new ArrayList<>();

    private final WrappedChannel wrapper = new WrappedChannel();

    public TerminalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        for (int i = 0; i < MAX_CHANNEL_SIZE; i++) {
            channels.add(new TerminalChannel(i));
        }

        buildRegistry(CHANNEL) .withBasic(CompoundTagPort.of(this::serializeChannels, this::deserializeChannels)).register();
        buildRegistry(WRAPPER) .withBasic(CompoundTagPort.of(wrapper::saveToTag, wrapper::loadFromTag)).register();
        buildRegistry(RECEIVER).withBasic(CompoundTagPort.of(remoteReceiver::serialize, remoteReceiver::deserialize)).register();
    }



    public void syncWithAttached(){
        BlockEntityGetter.getLevelBlockEntityAt(level, getBlockPos().relative(getDirection().getOpposite()), IReceiver.class)
                .map(IReceiver::receiver)
                .ifPresentOrElse(
                        remoteReceiver::adjustTo,
                        remoteReceiver::reset
                );
    }


    private void removeFromNetwork(){
        channels.forEach(e -> {
            REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(this.level, e);
            DECIMAL_LINK_NETWORK_HANDLER.removeFromNetwork(this.level, e);
        });
    }

    private void addToNetwork(){
        channels.forEach(e -> {
            REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(this.level, e);
            DECIMAL_LINK_NETWORK_HANDLER.addToNetwork(this.level, e);
        });
    }

    public void updateKeys(List<Couple<RedstoneLinkNetworkHandler.Frequency>> newKeys){
        removeFromNetwork();
        for(int i = 0; i < min(newKeys.size(), channels.size()); i++){
            channels.get(i).key = newKeys.get(i);
        }
        addToNetwork();
    }

    @Override
    public void tickServer() {
        super.tickServer();
        syncWithAttached();
    }

    public void openScreen(Player player){
        wrapper.overrideData(channels, getBlockPos(), remoteReceiver.validSize());
        NetworkHooks.openScreen((ServerPlayer) player, this, wrapper::write);
    }

    public String getAttachedDeviceName(){
        if(level == null || level.isClientSide)return "Should Not Called On Client";

        return BlockEntityGetter
                .getLevelBlockEntityAt(level, getBlockPos().relative(getDirection().getOpposite()), IReceiver.class)
                .map(IReceiver::name).orElse("Not Attached");
    }

    public void deviceChanged(){
        BlockEntityGetter
                .getLevelBlockEntityAt(level, getBlockPos().relative(getDirection().getOpposite()), BlockEntity.class)
                .ifPresent(BlockEntity::setChanged);
    }

    public void setMinMax(List<Couple<Double>> min_max){
        for(int i = 0; i < min(min_max.size(), this.channels.size()); i++){
            channels.get(i).setMinMax(min_max.get(i));
        }
        setChanged();
        deviceChanged();
    }

    public void setReversed(List<Boolean> row_reversed){
        for(int i = 0; i < min(row_reversed.size(), channels.size()); i++){
            channels.get(i).setReversed(row_reversed.get(i));
        }
        setChanged();
        deviceChanged();
    }

    public void setEnabled(List<Boolean> enabled){
        for(int i = 0; i < min(enabled.size(), this.channels.size()); i++){
            channels.get(i).setEnabled(enabled.get(i));
        }
        setChanged();
    }

    public void setFrequency(){
        List<Couple<RedstoneLinkNetworkHandler.Frequency>> newKeys = new ArrayList<>();
        for(int i = 0; i < channels.size(); i++){
            newKeys.add(toFrequency(wrapper, i));
        }
        updateKeys(newKeys);
        setChanged();
    }

    @Override
    public void initialize() {
        super.initialize();
        addToNetwork();
    }

    private CompoundTag serializeChannels(){
        CompoundTag wrap = new CompoundTag();
        CompoundTag channelTag = new CompoundTag();
        channels.forEach(e -> channelTag.put("channel_" + channels.indexOf(e), e.serialize()));
        wrap.put("channel tags", channelTag);
        return wrap;
    }

    private void deserializeChannels(CompoundTag wrap){
        CompoundTag channelTag = wrap.getCompound("channel tags");
        channels.forEach(e -> e.deserialize(channelTag.getCompound("channel_" + channels.indexOf(e))));
    }

    public static ItemStackHandler getFrequencyItems(WrappedChannel contentHolder) {
        ItemStackHandler newInv = new ItemStackHandler(2 * MAX_CHANNEL_SIZE);

        CompoundTag invNBT = contentHolder.inventoryTag().getCompound("items");
        if (!invNBT.isEmpty())
            newInv.deserializeNBT(invNBT);

        if (newInv.getSlots() != 2 * MAX_CHANNEL_SIZE){
            return new ItemStackHandler(2 * MAX_CHANNEL_SIZE);
        }

        return newInv;
    }

    public static Couple<RedstoneLinkNetworkHandler.Frequency> toFrequency(WrappedChannel controller, int slot /* 0 - 15 */) {
        ItemStackHandler frequencyItems = getFrequencyItems(controller);
        try {
            return Couple.create(
                    RedstoneLinkNetworkHandler.Frequency.of(frequencyItems.getStackInSlot(2 * slot    )),
                    RedstoneLinkNetworkHandler.Frequency.of(frequencyItems.getStackInSlot(2 * slot + 1))
            );
        }
        catch (IndexOutOfBoundsException e){
            return EMPTY_FREQUENCY;
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("_");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new TerminalMenu(ControlCraftMenuTypes.TERMINAL.get(), id, inv, wrapper);
    }

    public class TerminalChannel implements $IRedstoneLinkable {
        private Couple<RedstoneLinkNetworkHandler.Frequency> key = EMPTY_FREQUENCY;

        private boolean isReversed;
        private boolean enabled;


        private boolean isBoolean;
        private int lastAppliedSignal = 0;
        private int $lastAppliedSignal = 0; // decimal
        private final int index;

        public TerminalChannel(int control) {
            this.index = control;
        }


        @Override
        public int getTransmittedStrength() {
            return 0;
        }

        public void setBoolean(boolean aBoolean) {
            isBoolean = aBoolean;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
            remoteReceiver.get(index).enabled = enabled;
        }

        public void setReversed(boolean reversed) {
            isReversed = reversed;
        }

        public void setMinMax(Couple<Double> minMax){
            remoteReceiver.get(index).min_max = minMax;
        }

        @Override
        public boolean isListening() {
            return enabled;
        }

        @Override
        public boolean isAlive() {
            return !isRemoved();
        }

        @Override
        public void setReceivedStrength(int signal) {
            if(signal == lastAppliedSignal)return;
            if(isReversed && isBoolean)signal = 15 - signal;

            lastAppliedSignal = signal;
            remoteReceiver.accept(combine(), index);
        }

        public double combine(){
            return lastAppliedSignal + $lastAppliedSignal / 15.0;
        }

        @Override
        public void $setReceivedStrength(int signal) {
            if(signal == $lastAppliedSignal)return;
            if(isReversed && isBoolean)signal = 15 - signal;


            $lastAppliedSignal = signal;
            remoteReceiver.accept(combine(), index);
        }

        @Override
        public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
            return key;
        }

        @Override
        public BlockPos getLocation() {
            return getBlockPos();
        }


        public CompoundTag serialize(){
            return CompoundTagBuilder.create()
                    .withListTag("key", key.serializeEach(e -> e.getStack().serializeNBT()))
                    .withCompound("isReversed", SerializeUtils.BOOLEAN.serialize(isReversed))
                    .withCompound("enabled", SerializeUtils.BOOLEAN.serialize(enabled))
                    .withCompound("isBoolean", SerializeUtils.BOOLEAN.serialize(isBoolean))
                    .build();
        }

        public void deserialize(CompoundTag tag){
            try{
                key = Couple.deserializeEach(tag.getList("key", 10), e -> RedstoneLinkNetworkHandler.Frequency.of(ItemStack.of(e)));
                isReversed = SerializeUtils.BOOLEAN.deserialize(tag.getCompound("isReversed"));
                enabled = SerializeUtils.BOOLEAN.deserialize(tag.getCompound("enabled"));
                isBoolean = SerializeUtils.BOOLEAN.deserialize(tag.getCompound("isBoolean"));
            }catch (Exception e){
                ControlCraft.LOGGER.error("Some Slot didn't get properly deserialized {}", e.toString());
            }
        }

        public boolean isBoolean() {
            return isBoolean;
        }

        public boolean isReversed(){
            return isReversed;
        }

        public Couple<Double> getMinMax(){
            return remoteReceiver.get(index).min_max;
        }

        public SlotType getType(){
            return remoteReceiver.get(index).type;
        }

        public double latestValue(){
            return remoteReceiver.get(index).latestValue();
        }


    }
}
