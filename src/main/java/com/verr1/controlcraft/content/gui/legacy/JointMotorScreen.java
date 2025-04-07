package com.verr1.controlcraft.content.gui.legacy;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.utility.Components;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import com.verr1.controlcraft.registry.ControlCraftGuiLabels;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class JointMotorScreen extends ControllerScreen {


    private final boolean isAdjustingAngle;
    private final boolean isLocked;

    private final double offset;

    protected Label mLabel;

    private final boolean isCheatMode;

    protected EditBox oField;

    protected Label oLabel;

    protected Label cLabel;
    protected Label cField;
    protected Label lLabel;
    protected IconButton toggleCheatMode;
    protected IconButton toggleReverse;
    protected IconButton toggleSoftLock;

    public JointMotorScreen(
            BlockPos entityPos,
            double p,
            double i,
            double d,
            double v,
            double o,
            double t,
            boolean isAdjustingAngle,
            boolean isCheatMode,
            boolean isLocked
    ) {
        super(entityPos, p, i, d, v, t);
        this.isAdjustingAngle = isAdjustingAngle;
        this.isCheatMode = isCheatMode;
        this.isLocked = isLocked;
        this.offset = o;
    }

    @Override
    public void initWidgets() {
        super.initWidgets();
        oField = new EditBox(font, 0, 0, common_input_width, common_input_height, Components.literal("offset"));
        oField.setTextColor(-1);
        oField.setTextColorUneditable(-1);
        oField.setBordered(true);

        Component mode = isAdjustingAngle ? ExposedFieldType.MODE_ANGULAR.asComponent() : ExposedFieldType.MODE_SPEED.asComponent();
        mLabel = new Label(0, 0, mode).colored(common_label_color);
        mLabel.text = mode;
        addRenderableWidget(mLabel);

        cLabel = new Label(0, 0, ExposedFieldType.MODE_CHEAT.asComponent());
        cLabel.text = ExposedFieldType.MODE_CHEAT.asComponent();
        addRenderableWidget(cLabel);

        toggleCheatMode = new IconButton(0, 0, AllIcons.I_ARM_FORCED_ROUND_ROBIN);
        toggleCheatMode.withCallback(this::toggleCheatMode);
        toggleCheatMode.setToolTip(ControlCraftGuiLabels.cheatLabel);
        addRenderableWidget(toggleCheatMode);


        toggleReverse = new IconButton(0, 0, AllIcons.I_ARM_FORCED_ROUND_ROBIN);
        toggleReverse.withCallback(this::setToggleReverse);
        toggleReverse.setToolTip(Component.translatable(ControlCraft.MODID + ".tooltip.reverse"));
        addRenderableWidget(toggleReverse);

        toggleSoftLock = new IconButton(0, 0, AllIcons.I_CART_ROTATE_LOCKED);
        toggleSoftLock.withCallback(this::setToggleSoftLock);
        toggleSoftLock.setToolTip(Component.translatable(ControlCraft.MODID + ".tooltip.soft_lock"));
        addRenderableWidget(toggleSoftLock);

        Component onOff= isCheatMode ? ControlCraftGuiLabels.onLabel : ControlCraftGuiLabels.offLabel;
        cField = new Label(0, 0, onOff).colored((isCheatMode ? Color.RED : Color.GRAY).getRGB());
        cField.text = onOff;
        addRenderableWidget(cField);

        lLabel = new Label(0, 0, ExposedFieldType.IS_LOCKED$1.asComponent()).colored((isLocked ? Color.RED : Color.GREEN).getRGB());
        lLabel.text = isLocked ?
                Component.translatable(ControlCraft.MODID + ".screen.labels.locked") :
                Component.translatable(ControlCraft.MODID + ".screen.labels.free");
        addRenderableWidget(lLabel);


        oField.setMaxLength(35);
        oField.setValue(String.format("%.2f", offset));
        oField.setFilter(ParseUtils::tryParseDoubleFilter);

        addRenderableWidget(oField);

        oLabel = new Label(0, 0, ExposedFieldType.OFFSET.asComponent()).colored(common_label_color);
        oLabel.text = ExposedFieldType.OFFSET.asComponent();
        addRenderableWidget(oLabel);

    }

    public void toggleCheatMode(){
        var p = new BlockBoundServerPacket.builder(pos, RegisteredPacketType.TOGGLE_0)
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
        super.register();
        onClose();
    }

    public void setToggleReverse(){
        var p = new BlockBoundServerPacket.builder(pos, RegisteredPacketType.TOGGLE_1)
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
        // super.register();
        onClose();
    }

    public void setToggleSoftLock(){
        var p = new BlockBoundServerPacket.builder(pos, RegisteredPacketType.TOGGLE_2)
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
        // super.register();
        onClose();
    }

    @Override
    public void startWindow() {
        super.startWindow();
        statisticsLayout.addChild(mLabel, 1, 0);
        statisticsLayout.addChild(cLabel, 2, 0);
        statisticsLayout.addChild(cField, 2, 1);
        statisticsLayout.addChild(lLabel, 3, 0);
        buttonLayout.addChild(toggleCheatMode, 0, 3);
        buttonLayout.addChild(toggleReverse, 0, 4);
        buttonLayout.addChild(toggleSoftLock, 0, 5);
        controlValueLayout.rowSpacing(3);
        totalLayout.arrangeElements();

    }

    @Override
    public void register() {
        var p = new BlockBoundServerPacket.builder(pos, RegisteredPacketType.SETTING_0)
                .withDouble(ParseUtils.tryParseDouble(oField.getValue()))
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
        super.register();


    }

    @Override
    protected ItemStack renderedItem() {
        return ControlCraftBlocks.JOINT_MOTOR_BLOCK.asStack();
    }
}
