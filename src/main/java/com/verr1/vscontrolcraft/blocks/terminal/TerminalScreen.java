package com.verr1.vscontrolcraft.blocks.terminal;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.verr1.vscontrolcraft.registry.AllVSCCGuiTextures;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static java.lang.Math.min;

public class TerminalScreen extends AbstractSimiContainerScreen<TerminalMenu> {

    private final AllVSCCGuiTextures background = AllVSCCGuiTextures.SIMPLE_BACKGROUND_LARGE;
    private final AllGuiTextures slot = AllGuiTextures.JEI_SLOT;

    private BlockPos pos;
    private final String title;

    private final int rows;

    private final List<TerminalRowData> row_data = new ArrayList<>();

    private final List<EditBox> minFields = new ArrayList<>();
    private final List<EditBox> maxFields = new ArrayList<>();
    private final List<SmallCheckbox> toggleFields = new ArrayList<>();

    private final GridLayout layout = new GridLayout();

    private IconButton confirmButton;

    //BlockPos pos, String title, List<TerminalRowData> row_data

    public TerminalScreen(TerminalMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

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
                row_data.get(i).min_max().x(),
                row_data.get(i).min_max().y(),
                row_data.get(i).name(),
                row_data.get(i).isBoolean()
            );
        }
        layout.setX(leftPos + 8 + 40);
        layout.setY(topPos + 8 + 4);
        layout.rowSpacing(8).columnSpacing(3);
        layout.arrangeElements();
        /*
        confirmButton = new IconButton(x + background.width + 160, y + background.height - 20, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> {
            confirm();
            onClose();
        });

        addRenderableWidget(confirmButton);
        * */

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
                            new Vector2d(
                                    Util.tryParseDouble(minFields.get(i).getValue()),
                                    Util.tryParseDouble(maxFields.get(i).getValue())
                            ),
                            toggleFields.get(i).selected()
                    )
            );
        }
        TerminalSettingsPacket packet = new TerminalSettingsPacket(newSettings, pos);
        AllPackets.getChannel().sendToServer(packet);
    }

    public void initRow(int i, double row_min, double row_max, String row_name, boolean isBoolean){

        int len_y = 10;
        int input_len_x = 30;
        int title_len_x = 45;
        int min_max_len_x = 20;

        int color_label = new Color(250, 250, 90).getRGB();



        var name = new EditBox(font, 0, 0, title_len_x, len_y, Components.literal(""));
        name.setTextColor(-1);
        name.setTextColorUneditable(color_label);
        name.setBordered(false);
        name.setEditable(false);
        name.setMaxLength(title_len_x);
        name.setValue(row_name);

        var minTitle = new EditBox(font, 0, 0, min_max_len_x, len_y, Components.literal(""));
        minTitle.setTextColor(-1);
        minTitle.setTextColorUneditable(color_label);
        minTitle.setBordered(false);
        minTitle.setEditable(false);
        minTitle.setMaxLength(10);
        minTitle.setValue("min");
        minTitle.setFilter(Util::tryParseDoubleFilter);

        var minField = new EditBox(font, 0, 0, input_len_x, len_y, Components.literal(""));
        minField.setTextColor(-1);
        minField.setTextColorUneditable(-1);
        minField.setBordered(!isBoolean);
        minField.setEditable(!isBoolean);
        minField.setMaxLength(10);
        minField.setValue(isBoolean ? "0" : row_min + "");
        minField.setFilter(Util::tryParseDoubleFilter);

        var maxTitle = new EditBox(font, 0, 0, min_max_len_x, len_y, Components.literal(""));
        maxTitle.setTextColor(-1);
        maxTitle.setTextColorUneditable(color_label);
        maxTitle.setBordered(false);
        maxTitle.setEditable(false);
        maxTitle.setMaxLength(10);
        maxTitle.setValue("max");



        var maxField = new EditBox(font, 0, 0, input_len_x, len_y, Components.literal(""));
        maxField.setTextColor(-1);
        maxField.setTextColorUneditable(-1);
        maxField.setBordered(!isBoolean);
        maxField.setEditable(!isBoolean);
        maxField.setMaxLength(10);
        maxField.setValue(isBoolean ? "1" : row_max + "");
        maxField.setFilter(Util::tryParseDoubleFilter);


        var toggleField = new SmallCheckbox(0, 0, 10, 10, Components.literal(""), row_data.get(i).enabled());


        minFields.add(minField);
        maxFields.add(maxField);
        toggleFields.add(toggleField);
        // I need min max field, so don't return early

        layout.addChild(name, i, 0);
        layout.addChild(minTitle, i, 1);
        layout.addChild(minField, i, 2);
        layout.addChild(maxTitle, i, 3);
        layout.addChild(maxField, i, 4);
        layout.addChild(toggleField, i, 5);

        if(row_name.equals("empty"))return;
        addRenderableWidget(minField);
        addRenderableWidget(maxField);
        addRenderableWidget(name);
        addRenderableWidget(minTitle);
        addRenderableWidget(maxTitle);
        addRenderableWidget(toggleField);
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
