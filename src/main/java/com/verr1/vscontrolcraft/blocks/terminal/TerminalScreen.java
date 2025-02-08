package com.verr1.vscontrolcraft.blocks.terminal;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;
import static java.lang.Math.min;

public class TerminalScreen extends AbstractSimiContainerScreen<TerminalMenu> {

    private final AllGuiTextures background = PLAYER_INVENTORY;

    private BlockPos pos;
    private final String title;

    private final int rows;

    private final List<TerminalRowData> row_data = new ArrayList<>();

    private final List<EditBox> minFields = new ArrayList<>();
    private final List<EditBox> maxFields = new ArrayList<>();
    private final List<Checkbox> toggleFields = new ArrayList<>();

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
        //setWindowSize(background.width, background.height);
        setWindowSize(176, 60);
        super.init();

        int x = 60;
        int y = 10;

        for(int i = 0; i < rows; i++){
            initRow(i, x + 49, y + 14, row_data.get(i).min_max().x(), row_data.get(i).min_max().y(), row_data.get(i).name());
        }
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
        int invX = getLeftOfCentered(PLAYER_INVENTORY.width);
        int invY = topPos + 60 + 4;
        renderPlayerInventory(graphics, invX, invY);
        /*
        int x = leftPos;
        int y = topPos;

        background.render(graphics, x, y);
        graphics.drawString(font, title, x + 15, y + 4, 0x592424, false);

        GuiGameElement.of(menu.contentHolder).<GuiGameElement
                        .GuiRenderBuilder>at(x + background.width - 4, y + background.height - 56, -200)
                .scale(5)
                .render(graphics);
        * */

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

    public void initRow(int i, int base_x, int base_y, double row_min, double row_max, String row_name){

        int space_y = 15;
        int space_x = 4;
        int len_y = 10;
        int input_len_x = 30;
        int title_len_x = 35;
        int min_max_len_x = 20;

        int current_delta_x = 0;
        int common_row_y = base_y + i * (len_y + space_y);
        var newBox_name = new EditBox(font, base_x + current_delta_x, common_row_y, title_len_x, len_y, Components.literal(row_name + "max"));
        newBox_name.setTextColor(-1);
        newBox_name.setTextColorUneditable(-1);
        newBox_name.setBordered(false);
        newBox_name.setEditable(false);
        newBox_name.setMaxLength(10);
        newBox_name.setValue(row_name);
        newBox_name.setFilter(Util::tryParseDoubleFilter);
        current_delta_x += newBox_name.getWidth() + space_x;

        var newBox_min = new EditBox(font, base_x + current_delta_x, common_row_y, min_max_len_x, len_y, Components.literal(row_name + "max"));
        newBox_min.setTextColor(-1);
        newBox_min.setTextColorUneditable(-1);
        newBox_min.setBordered(false);
        newBox_min.setEditable(false);
        newBox_min.setMaxLength(10);
        newBox_min.setValue("min");
        newBox_min.setFilter(Util::tryParseDoubleFilter);
        current_delta_x += newBox_min.getWidth() + space_x;

        var minField = new EditBox(font, base_x + current_delta_x, common_row_y, input_len_x, len_y, Components.literal(row_name + "min"));
        minField.setTextColor(-1);
        minField.setTextColorUneditable(-1);
        minField.setBordered(false);
        minField.setMaxLength(10);
        minField.setValue(row_min + "");
        minField.setFilter(Util::tryParseDoubleFilter);
        current_delta_x += minField.getWidth() + space_x;

        var newBox_max = new EditBox(font, base_x + current_delta_x, common_row_y, min_max_len_x, len_y, Components.literal(row_name + "max"));
        newBox_max.setTextColor(-1);
        newBox_max.setTextColorUneditable(-1);
        newBox_max.setBordered(false);
        newBox_max.setEditable(false);
        newBox_max.setMaxLength(10);
        newBox_max.setValue("max");
        newBox_max.setFilter(Util::tryParseDoubleFilter);
        current_delta_x += newBox_max.getWidth() + space_x;


        var maxField = new EditBox(font, base_x + current_delta_x, common_row_y, input_len_x, len_y, Components.literal(row_name + "max"));
        maxField.setTextColor(-1);
        maxField.setTextColorUneditable(-1);
        maxField.setBordered(false);
        maxField.setMaxLength(10);
        maxField.setValue(row_max + "");
        maxField.setFilter(Util::tryParseDoubleFilter);
        current_delta_x += maxField.getWidth() + space_x;

        var toggleField = new Checkbox(base_x + current_delta_x, common_row_y - 5, 20, 20, Components.literal("enabled"), row_data.get(i).enabled());
        current_delta_x += toggleField.getWidth() + space_x;


        minFields.add(minField);
        maxFields.add(maxField);
        toggleFields.add(toggleField);
        // I need min max field, so don't return early
        if(row_name.equals("empty"))return;
        addRenderableWidget(minField);
        addRenderableWidget(maxField);
        addRenderableWidget(newBox_name);
        addRenderableWidget(newBox_min);
        addRenderableWidget(newBox_max);
        addRenderableWidget(toggleField);
    }

}
