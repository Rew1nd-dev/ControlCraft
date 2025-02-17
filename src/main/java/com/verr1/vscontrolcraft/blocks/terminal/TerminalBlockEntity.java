package com.verr1.vscontrolcraft.blocks.terminal;

import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.OnShipDirectinonalBlockEntity;
import com.verr1.vscontrolcraft.base.UltraTerminal.*;
import com.verr1.vscontrolcraft.registry.AllMenuTypes;
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
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

import static com.simibubi.create.Create.REDSTONE_LINK_NETWORK_HANDLER;
import static java.lang.Math.min;

public class TerminalBlockEntity extends OnShipDirectinonalBlockEntity implements
        MenuProvider
{
    public static final Couple<RedstoneLinkNetworkHandler.Frequency> EMPTY_FREQUENCY = Couple.create(
            RedstoneLinkNetworkHandler.Frequency.EMPTY,
            RedstoneLinkNetworkHandler.Frequency.EMPTY
    );




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

    private void clearChannelField(){
        channels.forEach(e -> e.setField(ExposedFieldWrapper.EMPTY));
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



    public TerminalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // addToNetwork();
    }

    public List<Vector2d> getMinMax(){
        return channels.stream().map(TerminalChannel::getMinMax).toList();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if(level.isClientSide)return;
        // updateKeys(channels.stream().map(TerminalChannel::getNetworkKey).toList());
    }

    public void syncAttachedDevice(){
        if(level.isClientSide)return;
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

    public List<ExposedFieldWrapper> getAttachedNumericFields() {
        return channels.stream().map(TerminalChannel::getField).toList();
    }

    public String getAttachedDeviceName(){
        BlockEntity be = level.getExistingBlockEntity(getBlockPos().relative(getDirection().getOpposite()));
        if(!(be instanceof ITerminalDevice device)) return "Not Attached";
        return device.name();
    }

    public void deviceChanged(){
        BlockEntity be = level.getExistingBlockEntity(getBlockPos().relative(getDirection().getOpposite()));
        if(be == null)return;
        be.setChanged();
    }

    public void setMinMax(List<Vector2d> min_max){
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

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        if(clientPacket)return;
        channels.forEach(e -> compound.put("channel_" + channels.indexOf(e), e.serialize()));
        compound.put("wrapper", wrapper.saveToTag());
        compound.putInt("exposedChannel", exposedChannel);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if(clientPacket)return;
        channels.forEach(e -> e.deserialize(compound.getCompound("channel_" + channels.indexOf(e))));
        wrapper.loadFromTag(compound.getCompound("wrapper"));
        exposedChannel = compound.getInt("exposedChannel");
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
        return new TerminalMenu(AllMenuTypes.TERMINAL.get(), id, inv, wrapper);
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
        private Vector2d min_max;
        private ExposedFieldWrapper attachedField;

        public boolean isReversed() {
            return isReversed;
        }

        public void setReversed(boolean reversed) {
            isReversed = reversed;
        }

        private boolean isReversed;

        public boolean isBoolean() {
            return isBoolean;
        }

        public void setBoolean(boolean aBoolean) {
            isBoolean = aBoolean;
        }

        private boolean isBoolean;

        private int lastAppliedSignal = 0;

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        private boolean enabled = false;

        public TerminalChannel(Couple<RedstoneLinkNetworkHandler.Frequency> key, ExposedFieldWrapper attachedField, boolean isBoolean) {
            this.key = key;
            this.min_max = new Vector2d(0, 1);
            this.attachedField = attachedField;
            this.isBoolean = isBoolean;
        }

        public void setKey(Couple<RedstoneLinkNetworkHandler.Frequency> key) {
            this.key = key;
        }

        public void setMinMax(Vector2d min_max){
            // this.attachedField.min_max = min_max;
            this.min_max = min_max;
        }

        public void setField(ExposedFieldWrapper field){
            this.attachedField = field;
        }

        public ExposedFieldWrapper getField(){
            return this.attachedField;
        }


        public Vector2d getMinMax(){
            // return attachedField.min_max;
            return min_max;
        }

        @Override
        public int getTransmittedStrength() {
            return 0;
        }

        @Override
        public void setReceivedStrength(int signal) {
            if(signal == lastAppliedSignal)return;  // && signal == 0
            lastAppliedSignal = signal;
            // attachedField.apply(power);
            if(isReversed && isBoolean)signal = 15 - signal;
            attachedField.field.apply(min_max.x + (min_max.y - min_max.x) * signal / 15);
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
            tag.putDouble("min", min_max.x);
            tag.putDouble("max", min_max.y);
            tag.putBoolean("isReversed", isReversed);
            return tag;
        }

        public void deserialize(CompoundTag tag){
            try{
                key = Couple.deserializeEach(tag.getList("key", 10), e -> RedstoneLinkNetworkHandler.Frequency.of(ItemStack.of(e)));
                isBoolean = tag.getBoolean("isBoolean");
                enabled = tag.getBoolean("enabled");
                min_max = new Vector2d(tag.getDouble("min"), tag.getDouble("max"));
                isReversed = tag.getBoolean("isReversed");
            }catch (Exception e){
                ControlCraft.LOGGER.info("Some Channel Didn't Get Properly Deserialized");
            }

        }

    }

}
