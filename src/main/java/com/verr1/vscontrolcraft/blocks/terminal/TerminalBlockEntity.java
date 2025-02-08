package com.verr1.vscontrolcraft.blocks.terminal;

import com.simibubi.create.content.redstone.link.IRedstoneLinkable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.vscontrolcraft.base.OnShipDirectinonalBlockEntity;
import com.verr1.vscontrolcraft.base.UltraTerminal.ITerminalDevice;
import com.verr1.vscontrolcraft.base.UltraTerminal.NumericField;
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

import java.nio.channels.Channel;
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


    private final ArrayList<TerminalChannel> channels = new ArrayList<>(
            List.of(
                    new TerminalChannel(EMPTY_FREQUENCY , new Vector2d(0, 1), NumericField.EMPTY),
                    new TerminalChannel(EMPTY_FREQUENCY , new Vector2d(0, 1), NumericField.EMPTY),
                    new TerminalChannel(EMPTY_FREQUENCY , new Vector2d(0, 1), NumericField.EMPTY),
                    new TerminalChannel(EMPTY_FREQUENCY , new Vector2d(0, 1), NumericField.EMPTY),
                    new TerminalChannel(EMPTY_FREQUENCY , new Vector2d(0, 1), NumericField.EMPTY),
                    new TerminalChannel(EMPTY_FREQUENCY , new Vector2d(0, 1), NumericField.EMPTY)
            )
    );

    private final ChannelWrapper wrapper = new ChannelWrapper();


    private void clearChannelField(){
        channels.forEach(e -> e.setField(NumericField.EMPTY));
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

    private void setChannelField(List<NumericField> fields){
        for(int i = 0; i < min(fields.size(), channels.size()); i++){
            channels.get(i).setField(fields.get(i));
        }
        for(int i = min(fields.size(), channels.size()); i < channels.size(); i++){
            channels.get(i).setField(NumericField.EMPTY);
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
        BlockEntity be = level.getExistingBlockEntity(getBlockPos().relative(getDirection().getOpposite()));
        if(!(be instanceof ITerminalDevice device))return;
        setChannelField(device.fields());
    }

    @Override
    public void tick() {
        super.tick();
        if(level.isClientSide)return;
        syncAttachedDevice();
    }

    public List<NumericField> getAttachedNumericFields() {
        return channels.stream().map(TerminalChannel::getField).toList();
    }

    public String getAttachedDeviceName(){
        BlockEntity be = level.getExistingBlockEntity(getBlockPos().relative(getDirection().getOpposite()));
        if(!(be instanceof ITerminalDevice device)) return "Not Attached";
        return device.name();
    }

    public void setMinMax(List<Vector2d> min_max){
        for(int i = 0; i < min(min_max.size(), this.channels.size()); i++){
            channels.get(i).setMinMax(min_max.get(i));
        }
    }

    public void setEnabled(List<Boolean> enabled){
        for(int i = 0; i < min(enabled.size(), this.channels.size()); i++){
            channels.get(i).setEnabled(enabled.get(i));
        }
    }

    public void setFrequency(){
        List<Couple<RedstoneLinkNetworkHandler.Frequency>> newKeys = new ArrayList<>();
        for(int i = 0; i < channels.size(); i++){
            newKeys.add(toFrequency(wrapper, i));
        }
        updateKeys(newKeys);
    }


    @Override
    public void initialize() {
        super.initialize();
        addToNetwork();
    }


    public void openScreen(Player player){
        wrapper.overrideData(getChannels(), getBlockPos(), getAttachedDeviceName());
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
        private NumericField attachedField;

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        private boolean enabled = true;

        public TerminalChannel(Couple<RedstoneLinkNetworkHandler.Frequency> key, Vector2d min_max, NumericField attachedField) {
            this.key = key;
            this.min_max = min_max;
            this.attachedField = attachedField;
        }

        public void setKey(Couple<RedstoneLinkNetworkHandler.Frequency> key) {
            this.key = key;
        }

        public void setMinMax(Vector2d min_max){
            this.min_max = min_max;
        }

        public void setField(NumericField field){
            this.attachedField = field;
        }

        public NumericField getField(){
            return this.attachedField;
        }


        public Vector2d getMinMax(){
            return min_max;
        }

        @Override
        public int getTransmittedStrength() {
            return 0;
        }

        @Override
        public void setReceivedStrength(int power) {
            attachedField.apply(min_max.x + (min_max.y - min_max.x) * power / 15);
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
    }

}
