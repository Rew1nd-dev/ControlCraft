package com.verr1.controlcraft.content.gui.layouts.preset;

import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.content.gui.factory.Converter;
import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.content.gui.layouts.element.TypedUIPort;
import com.verr1.controlcraft.content.gui.widgets.DescriptiveScrollInput;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import com.verr1.controlcraft.foundation.redstone.*;
import com.verr1.controlcraft.foundation.type.descriptive.*;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.verr1.controlcraft.content.gui.factory.Converter.convert;
import static com.verr1.controlcraft.foundation.redstone.IReceiver.FIELD_;

public class TerminalDeviceUIField extends TypedUIPort<CompoundTag> {


    private final DirectReceiver guiView = new DirectReceiver();

    private final List<SlotUI> slots = List.of(
            new SlotUI(),
            new SlotUI(),
            new SlotUI(),
            new SlotUI(),
            new SlotUI(),
            new SlotUI()
    );

    public DescriptiveScrollInput<SlotType> fieldSelector = new DescriptiveScrollInput<>(0, 0, 10, 10, ControlCraftGuiTextures.SMALL_BUTTON_SELECTION);
    public FormattedLabel fieldLabel = new FormattedLabel(0,0, Component.literal("      "));

    public DescriptiveScrollInput<GroupPolicy> policySelector = new DescriptiveScrollInput<>(0, 0, 10, 10, ControlCraftGuiTextures.SMALL_BUTTON_SELECTION, GroupPolicy.class);
    public FormattedLabel policyLabel = new FormattedLabel(0,0, Component.literal("      "));

    public DescriptiveScrollInput<LerpType> lerpSelector = new DescriptiveScrollInput<>(0, 0, 10, 10, ControlCraftGuiTextures.SMALL_BUTTON_SELECTION, LerpType.class);
    public FormattedLabel lerpLabel = new FormattedLabel(0,0, Component.literal("      "));


    // public IconButton confirm = new IconButton(0, 0,AllIcons.I_CONFIRM).withCallback(this::confirm);
    public int currentSize = 0;

    public DirectSlotGroup latestGroup = null;

    public void read(){
        lerpSelector.valueCalling(
                it -> lerpLabel.text = it.asComponent().copy().withStyle(Converter::optionStyle)
        );
        policySelector.valueCalling(
                it -> policyLabel.text = it.asComponent().copy().withStyle(Converter::optionStyle)
        );
        fieldSelector.withValues(
                guiView.view().stream().map(DirectSlotGroup::type).toArray(SlotType[]::new)
        );
        fieldSelector.calling(i -> {
            if(i >= guiView.view().size()){
                ControlCraft.LOGGER.error("Invalid index for field selector: " + i);
                return;
            }
            confirm();
            latestGroup = guiView.view().get(i);
            policySelector.setToValue(latestGroup.policy());
            lerpSelector.setToValue(latestGroup.lerpType());
            fieldLabel.setTextOnly(latestGroup.type().asComponent().copy().withStyle(Converter::optionStyle));
            currentSize = Math.min(latestGroup.view().size(), slots.size());
            for(int j = 0; j < currentSize; j++){
                slots.get(j).read(latestGroup.view().get(j));
            }
            if(isActivated)clampVisibility();
        });
        fieldSelector.onChanged();
        setMaxLength();
    }

    private void setMaxLength(){
        setMaxLength(fieldSelector, fieldLabel);
        setMaxLength(policySelector, policyLabel);
        setMaxLength(lerpSelector, lerpLabel);
    }

    private<T extends Enum<?> & Descriptive<?>> void setMaxLength(DescriptiveScrollInput<T> selector, FormattedLabel label){
        AtomicInteger maxLen = new AtomicInteger(0);
        selector.values().stream().map(Descriptive::asComponent).forEach(c -> {
            int len = Minecraft.getInstance().font.width(c.copy().withStyle(Converter::optionStyle));
            if(len > maxLen.get()) maxLen.set(len);
        });
        label.setWidth(maxLen.get());
    }

    @Override
    public void onMessage(Message msg) {
        if(msg.equals(Message.PRE_APPLY))confirm();
    }

    private void clampVisibility(){
        if(!isActivated)return;
        if(slots == null)return; // maybe called before slots are initialize at inner class
        int firstNoneDirectionIndex = slots
                .stream()
                .filter(s -> s.directionSelector.valueOfOption() == SlotDirection.NONE)
                .findFirst()
                .map(slots::indexOf)
                .orElse(currentSize);

        for (int i = 0; i < slots.size(); i++){
            SlotUI thisSlot = slots.get(i);
            boolean bl1 = i < currentSize;
            boolean bl2 = i < firstNoneDirectionIndex + 1;
            boolean bl3 = i < firstNoneDirectionIndex;
            thisSlot.setVisibility(
                    bl1 && bl2,
                    Optional.ofNullable(latestGroup)
                            .map(l -> !l.type().isBoolean() && bl1 && bl3)
                            .orElse(false)
            );
        }
        for(int i = firstNoneDirectionIndex + 1; i < slots.size(); i++){
            slots.get(i).reset();
            // slots.get(i).setVisibility(false, false);
        }

    }

    @Override
    public void onActivatedTab() {
        super.onActivatedTab();
        clampVisibility();
    }

    public void confirm(){
        if(latestGroup == null)return;
        latestGroup.setPolicy(policySelector.valueOfOption());
        latestGroup.setLerpType(lerpSelector.valueOfOption());
        for(int j = 0; j < Math.min(currentSize, latestGroup.view().size()); j++){
            slots.get(j).write(latestGroup.view().get(j));
        }
    }


    public TerminalDeviceUIField(BlockPos boundPos) {
        super(boundPos, FIELD_, CompoundTag.class, new CompoundTag());
    }

    @Override
    protected void initLayout(GridLayout layoutToFill) {
        int i = 0;

        GridLayout fieldSelectLayout = new GridLayout();
        fieldSelectLayout.addChild(fieldLabel,    0, 0);
        fieldSelectLayout.addChild(fieldSelector, 0, 1);
        fieldSelectLayout.addChild(policyLabel, 1, 0);
        fieldSelectLayout.addChild(policySelector, 1, 1);
        fieldSelectLayout.addChild(lerpLabel, 1, 2);
        fieldSelectLayout.addChild(lerpSelector, 1, 3);
        fieldSelectLayout.rowSpacing(4).columnSpacing(4);

        layoutToFill.addChild(fieldSelectLayout, 0, 0);
        i++;
        for (SlotUI slot : slots) {
            layoutToFill.addChild(slot.createLayout(), i++, 0);
        }

        // layoutToFill.addChild(confirm, i, 0);

        layoutToFill.columnSpacing(2).rowSpacing(3);
    }

    @Override
    protected CompoundTag readGUI() {
        return guiView.serialize();
    }

    @Override
    protected void writeGUI(CompoundTag value) {
        guiView.deserializeClientView(value);
        read();
    }


    class SlotUI{

        public Label minLabel = convert(UIContents.MIN, Converter::titleStyle).toDescriptiveLabel();
        public Label maxLabel = convert(UIContents.MAX, Converter::titleStyle).toDescriptiveLabel();

        public FormattedLabel directionLabel = new FormattedLabel(0, 0, Component.literal("      "));
        public DescriptiveScrollInput<SlotDirection> directionSelector = new
                DescriptiveScrollInput<>(0, 0, 10, 10, ControlCraftGuiTextures.SMALL_BUTTON_SELECTION, SlotDirection.class)
                .valueCalling(
                        it -> {
                            directionLabel.text = it.asComponent().copy().withStyle(Converter::optionStyle);
                            clampVisibility();
                });

        public EditBox minField = new EditBox(Minecraft.getInstance().font, 0, 0, 30, 10, Component.empty());
        public EditBox maxField = new EditBox(Minecraft.getInstance().font, 0, 0, 30, 10, Component.empty());

        private Couple<Double> initialMinMax = Couple.create(0.0, 1.0);

        public SlotUI(){

            AtomicInteger maxLen = new AtomicInteger(0);
            directionSelector.values().stream().map(SlotDirection::asComponent).forEach(c -> {
                int len = Minecraft.getInstance().font.width(c.copy().withStyle(Converter::optionStyle));
                if(len > maxLen.get()) maxLen.set(len);
            });
            directionLabel.setWidth(maxLen.get());
            directionSelector.onChanged();
        }


        public void read(DirectSlotControl slot){
            directionSelector.setToValue(slot.direction);
            minField.setValue("%.2f".formatted(slot.min_max.getFirst()));
            maxField.setValue("%.2f".formatted(slot.min_max.getSecond()));
            initialMinMax = slot.min_max;
        }

        public void write(DirectSlotControl slot){
            slot.direction = directionSelector.valueOfOption();
            if(slot.type().isBoolean())return;
            slot.min_max = Couple.create(
                    ParseUtils.tryParseDouble(minField.getValue()),
                    ParseUtils.tryParseDouble(maxField.getValue())
            );
        }

        /*
        * public void setVisibility(boolean visible, boolean isBooleanType){

            directionLabel.visible = visible;
            directionSelector.visible = visible;

            minLabel.visible = visible && !isBooleanType;
            maxLabel.visible = visible && !isBooleanType;
            minField.visible = visible && !isBooleanType;
            maxField.visible = visible && !isBooleanType;


        }
        * */

        public void setVisibility(boolean dir, boolean field){

            directionLabel.visible = dir;
            directionSelector.visible = dir;

            minLabel.visible = field;
            maxLabel.visible = field;
            minField.visible = field;
            maxField.visible = field;


        }

        public GridLayout createLayout(){
            GridLayout layout = new GridLayout();

            layout.addChild(directionLabel, 0, 0);
            layout.addChild(directionSelector, 0, 1);

            layout.addChild(minLabel, 0, 2);
            layout.addChild(minField, 0, 3);
            layout.addChild(maxLabel, 0, 4);
            layout.addChild(maxField, 0, 5);

            layout.rowSpacing(2);
            layout.columnSpacing(2);
            return layout;
        }

        public void reset(){
            directionSelector.setToValueOnly(SlotDirection.NONE); // onChange() will cause a loop
            directionLabel.text = SlotDirection.NONE.asComponent().copy().withStyle(Converter::optionStyle);

            minField.setValue(initialMinMax.get(true) + "");
            maxField.setValue(initialMinMax.get(false) + "");
        }

    }

}
