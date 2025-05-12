package com.verr1.controlcraft.content.legacy;


import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.content.gui.widgets.SmallCheckbox;
import com.verr1.controlcraft.foundation.data.terminal.TerminalRowData;
import com.verr1.controlcraft.foundation.data.terminal.TerminalRowSetting;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import com.verr1.controlcraft.foundation.type.descriptive.MiscDescription;
import com.verr1.controlcraft.foundation.type.descriptive.UIContents;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

public class TerminalScreen_ extends AbstractSimiContainerScreen<TerminalMenu__> {

    private final ControlCraftGuiTextures background = ControlCraftGuiTextures.SIMPLE_BACKGROUND_LARGE;
    private final AllGuiTextures slot = AllGuiTextures.JEI_SLOT;

    private BlockPos pos;
    private final String title;

    private final int rows;

    private final int exposedIndex;

    private final List<TerminalRowData> row_data = new ArrayList<>();

    private final List<EditBox> minFields = new ArrayList<>();
    private final List<EditBox> maxFields = new ArrayList<>();
    private final List<SmallCheckbox> isReversedFields = new ArrayList<>();
    private final List<SmallCheckbox> toggleFields = new ArrayList<>();

    private final List<SmallCheckbox> selectAsOutputButtons = new ArrayList<>();

    private final GridLayout layout = new GridLayout();

    private IconButton confirmButton;

    //BlockPos pos, String title, List<TerminalRowData> row_data

    public TerminalScreen_(TerminalMenu__ menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.exposedIndex = menu.contentHolder.getExposedIndex();
        this.pos = menu.contentHolder.getPos();
        this.title = menu.contentHolder.getTitle();
        this.row_data.addAll(menu.contentHolder.getRow_data());
        this.rows = row_data.size();
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


        for(int i = 0; i < rows; i++){
            initRow(
                    i,
                    row_data.get(i).min_max().get(true),
                    row_data.get(i).min_max().get(false),
                    row_data.get(i).type(),
                    row_data.get(i).isBoolean()
            );
        }
        layout.setX(leftPos + 8 + 40);
        layout.setY(topPos + 8 + 4);
        layout.rowSpacing(8).columnSpacing(6);
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
        List<TerminalRowSetting> newSettings = new ArrayList<>();
        for(int i = 0; i < rows; i++){
            newSettings.add(
                    new TerminalRowSetting(
                            Couple.create(
                                    ParseUtils.tryParseDouble(minFields.get(i).getValue()),
                                    ParseUtils.tryParseDouble(maxFields.get(i).getValue())
                            ),
                            toggleFields.get(i).selected(),
                            isReversedFields.get(i).selected()
                    )
            );
        }
        TerminalSettingsPacket__ packet = new TerminalSettingsPacket__(newSettings, pos, getExposedIndex());
        ControlCraftPackets.getChannel().sendToServer(packet);
    }

    public int getExposedIndex(){
        for(int i = 0; i < selectAsOutputButtons.size(); i++){
            if(selectAsOutputButtons.get(i).selected())return i;
        }
        return -1;
    }

    public void initRow(int i, double row_min, double row_max, SlotType row_type, boolean isBoolean){

        int len_y = 10;
        int input_len_x = 30;
        int title_len_x = 45;
        int min_max_len_x = 30;

        int color_label = new Color(44, 101, 122, 255).getRGB();



        var name = row_type.toDescriptiveLabel();

        var minTitle = UIContents.MIN.toDescriptiveLabel();


        var minField = new EditBox(font, 0, 0, input_len_x, len_y, Components.literal(""));
        minField.setTextColor(-1);
        minField.setTextColorUneditable(-1);
        minField.setBordered(!isBoolean);
        minField.setEditable(!isBoolean);
        minField.setMaxLength(10);
        minField.setValue(isBoolean ? "0" : String.format("%.2f", row_min));
        minField.setFilter(ParseUtils::tryParseDoubleFilter);

        var maxTitle = UIContents.MAX.toDescriptiveLabel();



        var maxField = new EditBox(font, 0, 0, input_len_x, len_y, Components.literal(""));
        maxField.setTextColor(-1);
        maxField.setTextColorUneditable(-1);
        maxField.setBordered(!isBoolean);
        maxField.setEditable(!isBoolean);
        maxField.setMaxLength(10);
        maxField.setValue(isBoolean ? "1" : String.format("%.2f", row_max));
        maxField.setFilter(ParseUtils::tryParseDoubleFilter);


        var toggleField = new SmallCheckbox(0, 0, 10, 10, MiscDescription.TURN_ON.specific().get(0), row_data.get(i).enabled());

        var toggleReverse = new SmallCheckbox(0, 0, 10, 10, MiscDescription.REVERSE_INPUT.specific().get(0), row_data.get(i).isReversed());

        var select = new SmallCheckbox(0, 0, 10, 10, MiscDescription.AS_REDSTONE_INPUT.specific().get(0), i == exposedIndex).withCallback(
                (self) -> {
                    for(int j = 0; j < selectAsOutputButtons.size(); j++){
                        if(j == i)break;
                        selectAsOutputButtons.get(j).setSelected(false);
                    }
                    return false;
                }
        );

        if(row_type.isBoolean()){
            minField.visible = false;
            maxField.visible = false;
            minTitle.visible = false;
            maxTitle.visible = false;
        }else{
            toggleReverse.visible = false;
        }

        minFields.add(minField);
        maxFields.add(maxField);
        toggleFields.add(toggleField);
        isReversedFields.add(toggleReverse);
        selectAsOutputButtons.add(select);
        // I need min max field, so don't return early

        layout.addChild(name, i, 0);

        layout.addChild(minTitle, i, 1);
        layout.addChild(minField, i, 2);
        layout.addChild(maxTitle, i, 3);
        layout.addChild(maxField, i, 4);

        layout.addChild(toggleReverse, i, 1);

        layout.addChild(toggleField, i, 5);
        layout.addChild(select, i, 6);

        if(row_type.equals(SlotType.NONE))return;
        addRenderableWidget(minField);
        addRenderableWidget(maxField);
        addRenderableWidget(name);
        addRenderableWidget(minTitle);
        addRenderableWidget(maxTitle);
        addRenderableWidget(toggleField);
        addRenderableWidget(toggleReverse);
        addRenderableWidget(select);
    }

    private void renderFrequencySlot(GuiGraphics graphics){
        int x = leftPos + 8;
        int y = topPos + 8;

        for(int row = 0; row < 6; row++){
            for(int column = 0; column < 2; column++){
                slot.render(graphics, x + column * 18, y + row * 18);
            }
        }


    }

}
