package com.verr1.controlcraft.content.gui.screens;


import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.content.gui.widgets.DescriptiveScrollInput;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import com.verr1.controlcraft.content.gui.widgets.IconSelectionScrollInput;
import com.verr1.controlcraft.content.gui.widgets.SmallCheckbox;
import com.verr1.controlcraft.foundation.data.terminal.TerminalRowData;
import com.verr1.controlcraft.foundation.data.terminal.TerminalRowSetting;
import com.verr1.controlcraft.foundation.network.packets.specific.TerminalSettingsPacket;
import com.verr1.controlcraft.foundation.redstone.TerminalMenu;
import com.verr1.controlcraft.foundation.type.descriptive.MiscDescription;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import com.verr1.controlcraft.foundation.type.descriptive.UIContents;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.MinecraftUtils;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

public class TerminalScreen extends AbstractSimiContainerScreen<TerminalMenu> {
    public static final int LINE_BLOCK = 6;

    static class MutableLine{
        Couple<Double> minMax;
        final boolean isBoolean;
        boolean isOn;
        boolean isReversed;
        final SlotType type;

        MutableLine(TerminalRowData immutableData) {
            this.type = immutableData.type();
            this.minMax = immutableData.min_max();
            this.isBoolean = immutableData.isBoolean();
            this.isReversed = immutableData.isReversed();
            this.isOn = immutableData.enabled();
        }

        public TerminalRowSetting immutable(){
            return new TerminalRowSetting(
                    minMax,
                    isOn,
                    isReversed
            );
        }

    }

    static class MutableBlock{
        List<MutableLine> lines = new ArrayList<>();

        public MutableBlock(List<TerminalRowData> blockData) {
            for(TerminalRowData line : blockData){
                lines.add(new MutableLine(line));
            }
        }


    }

    static class LineUI {

        FormattedLabel name;
        FormattedLabel minTitle;
        FormattedLabel maxTitle;
        EditBox minField;
        EditBox maxField;
        SmallCheckbox toggleField;
        SmallCheckbox toggleReverse;
        final int labelMaxLen;

        public LineUI(@NotNull  Font font, int labelMaxLen) {
            this.labelMaxLen = labelMaxLen;

            int input_len_x = 30;
            int len_y = 10;

            name = UIContents.NAME.toUILabel(); // will be modified
            minTitle = UIContents.MIN.toDescriptiveLabel();
            minField = new EditBox(font, 0, 0, input_len_x, len_y, Components.literal(""));
            maxTitle = UIContents.MAX.toDescriptiveLabel();
            maxField = new EditBox(font, 0, 0, input_len_x, len_y, Components.literal(""));
            toggleField = new SmallCheckbox(0, 0, 10, 10, MiscDescription.TURN_ON.specific().get(0), false);
            toggleReverse = new SmallCheckbox(0, 0, 10, 10, MiscDescription.REVERSE_INPUT.specific().get(0), false);

            minField.setFilter(ParseUtils::tryParseDoubleFilter);
            maxField.setFilter(ParseUtils::tryParseDoubleFilter);
            name.setWidth(labelMaxLen);
        }

        public void read(MutableLine data){

            name.setTextOnly(data.type.asComponent());

            minField.setValue(String.format("%.2f", data.minMax.get(true)));
            maxField.setValue(String.format("%.2f", data.minMax.get(false)));
            toggleField.setSelected(data.isOn);
            toggleReverse.setSelected(data.isReversed);

            setVisible(!data.type.isBoolean(), data.type.isBoolean(), true, true);

        }

        public void write(MutableLine data){
            data.minMax = Couple.create(
                    ParseUtils.tryParseDouble(minField.getValue()),
                    ParseUtils.tryParseDouble(maxField.getValue())
            );
            data.isOn = toggleField.selected();
            data.isReversed = toggleReverse.selected();
        }



        public void setVisible(boolean field, boolean reverse, boolean nameF, boolean toggle){
            minField.visible = field;
            maxField.visible = field;
            minTitle.visible = field;
            maxTitle.visible = field;
            toggleField.visible = toggle;
            toggleReverse.visible = reverse;
            name.visible = nameF;
        }



        public GridLayout createUI(){
            GridLayout lineLayout = new GridLayout();
            lineLayout.addChild(name, 0, 0);
            lineLayout.addChild(minTitle, 0, 1);
            lineLayout.addChild(minField, 0, 2);
            lineLayout.addChild(maxTitle, 0, 3);
            lineLayout.addChild(maxField, 0, 4);
            lineLayout.addChild(toggleReverse, 0, 5);
            lineLayout.addChild(toggleField, 0, 6);
            lineLayout.columnSpacing(6);
            return lineLayout;
        }
    }

    static class BlockUI{
        private final List<LineUI> lines = new ArrayList<>();
        private int currentValidSize = 0;

        BlockUI(@NotNull Font font, int labelMaxLen){
            for(int i = 0; i < LINE_BLOCK; i++){
                lines.add(new LineUI(font, labelMaxLen));
            }
        }

        public void read(MutableBlock data){
            int lineCount = Math.min(lines.size(), data.lines.size());
            for(int i = 0; i < lineCount; i++){
                lines.get(i).read(data.lines.get(i));
            }
            currentValidSize = lineCount;
            for (int i = lineCount; i < lines.size(); i++){
                lines.get(i).setVisible(false, false, false, false);
            }
        }

        public int size(){
            return currentValidSize;
        }

        public void write(MutableBlock data){
            int lineCount = Math.min(lines.size(), data.lines.size());
            for(int i = 0; i < lineCount; i++){
                lines.get(i).write(data.lines.get(i));
            }
        }


        public GridLayout createLayout(){
            GridLayout blockLayout = new GridLayout();
            for(int i = 0; i < lines.size(); i++){
                blockLayout.addChild(lines.get(i).createUI(), i, 0);
            }
            blockLayout.rowSpacing(8);
            return blockLayout;
        }

    }


    private final List<TerminalRowData> immutableData;
    private final ControlCraftGuiTextures background = ControlCraftGuiTextures.SIMPLE_BACKGROUND_LARGE;
    private final AllGuiTextures slot = AllGuiTextures.JEI_SLOT;



    private final BlockPos pos;


    private final List<MutableBlock> data = new ArrayList<>();

    private final BlockUI block;


    private final GridLayout layout = new GridLayout();

    private int currentIndex = 0;

    private final IconSelectionScrollInput blockSelector = (IconSelectionScrollInput)
            new IconSelectionScrollInput(
                    0, 0, 10, 10,
                    ControlCraftGuiTextures.SMALL_BUTTON_SELECTION
            ).calling(this::setBlock);


    public TerminalScreen(TerminalMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.pos = menu.contentHolder.getPos();
        this.immutableData = menu.contentHolder.data().subList(0, menu.contentHolder.validSize());
        int initialSize = immutableData.size();

        int labelMaxLen = MinecraftUtils.maxLength(immutableData.stream().map(TerminalRowData::type).toList());


        block = new BlockUI(Minecraft.getInstance().font, labelMaxLen);

        int pages = (initialSize - 1) / LINE_BLOCK + 1;
        for (int i = 0; i < pages; i++) {
            int start = i * LINE_BLOCK;
            int end = Math.min(start + LINE_BLOCK, initialSize);
            data.add(new MutableBlock(immutableData.subList(start, end)));
        }

        blockSelector.forOptions(
                IntStream.range(0, pages)
                        .mapToObj(i -> Components.literal("Page " + (i + 1)))
                        .toList()
        );

        read(0);
        currentIndex = 0;
        menu.setPage(0);
    }

    private boolean validIndex(int index){
        return index >= 0 && index < data.size();
    }

    private void setBlock(int index){
        if(!validIndex(index))return;
        writeCurrent();
        read(index);
        currentIndex = index;
        menu.setPage(index);
    }

    private void writeCurrent(){
        if(validIndex(currentIndex)) block.write(data.get(currentIndex));
    }

    private void read(int index){
        block.read(data.get(index));
    }


    @Override
    public void init(){
        setWindowSize(
                Math.max(
                        background.width,
                        PLAYER_INVENTORY.width
                ),
                background.height + 4 + PLAYER_INVENTORY.height
        );
        super.init();


        layout.addChild(block.createLayout(), 0, 0);
        layout.addChild(blockSelector, 1, 0);
        layout.setX(leftPos + 8 + 40);
        layout.setY(topPos + 8 + 4);

        layout.visitWidgets(this::addRenderableWidget);
        layout.arrangeElements();

    }

    @Override
    public void onClose() {
        super.onClose();
        confirm();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float p_97788_, int p_97789_, int p_97790_) {
        // debugWindowArea(graphics);

        int invX = getLeftOfCentered(PLAYER_INVENTORY.width);
        int invY = topPos + background.height + 4;
        renderPlayerInventory(graphics, invX, invY);

        int x = leftPos;
        int y = topPos;

        background.render(graphics, x, y);
        renderFrequencySlot(graphics);

    }

    public void confirm(){
        writeCurrent();
        List<TerminalRowSetting> newSettings = new ArrayList<>();
        for (MutableBlock block : data) {
            for (int j = 0; j < block.lines.size(); j++) {
                MutableLine line = block.lines.get(j);
                newSettings.add(line.immutable());
            }
        }
        TerminalSettingsPacket packet = new TerminalSettingsPacket(newSettings, pos);
        ControlCraftPackets.getChannel().sendToServer(packet);
    }

    private void renderFrequencySlot(GuiGraphics graphics){
        int x = leftPos + 8;
        int y = topPos + 8;

        for(int row = 0; row < block.size(); row++){
            for(int column = 0; column < 2; column++){
                slot.render(graphics, x + column * 18, y + row * 18);
            }
        }


    }

}
