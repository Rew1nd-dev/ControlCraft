package com.verr1.controlcraft.content.blocks.terminal;

import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.network.executors.CompoundTagPort;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.content.gui.screens.TerminalMenu;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.api.delegate.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.registry.ControlCraftMenuTypes;
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
import static java.lang.Math.min;

public class TerminalBlockEntity extends OnShipBlockEntity implements
        MenuProvider
{
    public static final Couple<RedstoneLinkNetworkHandler.Frequency> EMPTY_FREQUENCY = Couple.create(
            RedstoneLinkNetworkHandler.Frequency.EMPTY,
            RedstoneLinkNetworkHandler.Frequency.EMPTY
    );

    public static final NetworkKey EXPOSED_CHANNEL = NetworkKey.create("exposed_channel");
    public static final NetworkKey CHANNEL = NetworkKey.create("channel");
    public static final NetworkKey WRAPPER = NetworkKey.create("wrapper");

    private int exposedChannel = -1;

    private final ArrayList<TerminalChannel> channels = new ArrayList<>(
            List.of(
                    new TerminalChannel(EMPTY_FREQUENCY , ExposedFieldWrapper.EMPTY, false),
                    new TerminalChannel(EMPTY_FREQUENCY , ExposedFieldWrapper.EMPTY, false),
                    new TerminalChannel(EMPTY_FREQUENCY , ExposedFieldWrapper.EMPTY, false),
                    new TerminalChannel(EMPTY_FREQUENCY , ExposedFieldWrapper.EMPTY, false),
                    new TerminalChannel(EMPTY_FREQUENCY , ExposedFieldWrapper.EMPTY, false),
                    new TerminalChannel(EMPTY_FREQUENCY , ExposedFieldWrapper.EMPTY, false)
            )
    );

    private final ChannelWrapper wrapper = new ChannelWrapper();

    public void accept(int directSignal){
        if(!(exposedChannel < channels.size() && exposedChannel >= 0))return;
        channels.get(exposedChannel).setReceivedStrength(directSignal);
    }

    public void setExposedChannel(int exposedChannel) {
        this.exposedChannel = exposedChannel;
    }


    public List<TerminalChannel> getChannels(){
        return channels;
    }

    private void removeFromNetwork(){
        channels.forEach(e -> REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(this.level, e));
    }

    private void addToNetwork(){
        channels.forEach(e -> REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(this.level, e));
    }

    public void updateKeys(List<Couple<RedstoneLinkNetworkHandler.Frequency>> newKeys){
        removeFromNetwork();
        for(int i = 0; i < min(newKeys.size(), channels.size()); i++){
            channels.get(i).key = newKeys.get(i);
        }
        addToNetwork();
    }

    private void setChannelField(List<ExposedFieldWrapper> fields){
        for(int i = 0; i < min(fields.size(), channels.size()); i++){
            channels.get(i).setField(fields.get(i));
            channels.get(i).setBoolean(fields.get(i).type.isBoolean());
        }
        for(int i = min(fields.size(), channels.size()); i < channels.size(); i++){
            channels.get(i).setField(ExposedFieldWrapper.EMPTY);
        }
    }


    public void syncAttachedDevice(){
        if(level == null || level.isClientSide)return;
        BlockEntity be = level.getExistingBlockEntity(getBlockPos().relative(getDirection().getOpposite()));
        if(!(be instanceof ITerminalDevice device))return;
        setChannelField(device.fields());
        // setMinMax(device.fields().stream().map(e -> e.min_max).toList());
    }

    @Override
    public void tick() {
        super.tick();
        syncAttachedDevice();
    }

    public String getAttachedDeviceName(){
        if(level == null || level.isClientSide)return "Should Not Called On Client";

        return BlockEntityGetter
                .getLevelBlockEntityAt(level, getBlockPos().relative(getDirection().getOpposite()), ITerminalDevice.class)
                .map(ITerminalDevice::name).orElse("Not Attached");
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

    public TerminalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        buildRegistry(EXPOSED_CHANNEL).withBasic(SerializePort.of(() -> exposedChannel, i -> exposedChannel = i, SerializeUtils.INT)).register();
        buildRegistry(CHANNEL).withBasic(CompoundTagPort.of(this::serializeChannels, this::deserializeChannels)).register();
        buildRegistry(WRAPPER).withBasic(CompoundTagPort.of(wrapper::saveToTag, wrapper::loadFromTag)).register();

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

    public void openScreen(Player player){
        wrapper.overrideData(getChannels(), getBlockPos(), getAttachedDeviceName(), exposedChannel);
        NetworkHooks.openScreen((ServerPlayer) player, this, wrapper::write);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("terminal");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new TerminalMenu(ControlCraftMenuTypes.TERMINAL.get(), id, inv, wrapper);
    }


    public static ItemStackHandler getFrequencyItems(ChannelWrapper contentHolder) {
        ItemStackHandler newInv = new ItemStackHandler(12);

        CompoundTag invNBT = contentHolder.getInventoryTag().getCompound("items");
        if (!invNBT.isEmpty())
            newInv.deserializeNBT(invNBT);
        return newInv;
    }

    public static Couple<RedstoneLinkNetworkHandler.Frequency> toFrequency(ChannelWrapper controller, int slot /* 0 - 5 */) {
        ItemStackHandler frequencyItems = getFrequencyItems(controller);
        return Couple.create(
                RedstoneLinkNetworkHandler.Frequency.of(frequencyItems.getStackInSlot(slot)),
                RedstoneLinkNetworkHandler.Frequency.of(frequencyItems.getStackInSlot(slot + 6))
        );
    }


    public class TerminalChannel implements IRedstoneLinkable {
        private Couple<RedstoneLinkNetworkHandler.Frequency> key;
        private Couple<Double> min_max;
        private ExposedFieldWrapper attachedField;
        private boolean isReversed;
        private boolean isBoolean;
        private int lastAppliedSignal = 0;


        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        private boolean enabled = false;

        public TerminalChannel(Couple<RedstoneLinkNetworkHandler.Frequency> key, ExposedFieldWrapper attachedField, boolean isBoolean) {
            this.key = key;
            this.min_max = Couple.create(0.0, 1.0);
            this.attachedField = attachedField;
            this.isBoolean = isBoolean;
        }


        public boolean isReversed() {
            return isReversed;
        }

        public void setReversed(boolean reversed) {
            isReversed = reversed;
        }


        public boolean isBoolean() {
            return isBoolean;
        }

        public void setBoolean(boolean aBoolean) {
            isBoolean = aBoolean;
        }

        public void setKey(Couple<RedstoneLinkNetworkHandler.Frequency> key) {
            this.key = key;
        }

        public void setMinMax(Couple<Double> min_max){
            this.min_max = min_max;
        }

        public void setField(ExposedFieldWrapper field){
            this.attachedField = field;
        }

        public ExposedFieldWrapper getField(){
            return this.attachedField;
        }


        public Couple<Double> getMinMax(){
            return min_max;
        }

        @Override
        public int getTransmittedStrength() {
            return 0;
        }

        @Override
        public void setReceivedStrength(int signal) {
            if(signal == lastAppliedSignal)return;
            lastAppliedSignal = signal;
            if(isReversed && isBoolean)signal = 15 - signal;
            double min = min_max.get(true);
            double max = min_max.get(false);

            attachedField.field.apply(min + (max - min) * signal / 15);
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
        public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
            return this.key;
        }

        @Override
        public BlockPos getLocation() {
            return getBlockPos();
        }

        public CompoundTag serialize(){
            CompoundTag tag = new CompoundTag();
            tag.put("key", key.serializeEach(e -> e.getStack().serializeNBT()));
            tag.putBoolean("isBoolean", isBoolean);
            tag.putBoolean("enabled", enabled);
            tag.putDouble("min", min_max.get(true));
            tag.putDouble("max", min_max.get(false));
            tag.putBoolean("isReversed", isReversed);
            return tag;
        }

        public void deserialize(CompoundTag tag){
            try{
                key = Couple.deserializeEach(tag.getList("key", 10), e -> RedstoneLinkNetworkHandler.Frequency.of(ItemStack.of(e)));
                isBoolean = tag.getBoolean("isBoolean");
                enabled = tag.getBoolean("enabled");
                min_max = Couple.create(tag.getDouble("min"), tag.getDouble("max")); // new Vector2d(tag.getDouble("min"), tag.getDouble("max"));
                isReversed = tag.getBoolean("isReversed");
            }catch (Exception e){
                ControlCraft.LOGGER.info("Some Channel Didn't Get Properly Deserialized");
            }

        }

    }

}
