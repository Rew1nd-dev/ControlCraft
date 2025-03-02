package com.verr1.vscontrolcraft.blocks.servoMotor;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.utility.Components;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerScreen;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllGuiLabels;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class ServoMotorScreen extends PIDControllerScreen {

    private final double offset;
    private final boolean isAdjustingAngle;
    private final boolean isCheatMode;
    private final boolean isLocked;

    protected EditBox oField;

    protected Label oLabel;
    protected Label mLabel;
    protected Label cLabel;
    protected Label cField;
    protected Label lLabel;

    protected IconButton toggleCheatMode;
    protected IconButton toggleReverse;
    protected IconButton toggleSoftLock;

    public ServoMotorScreen(
            BlockPos entityPos,
            double p,
            double i,
            double d,
            double v,
            double t,
            double offset,
            boolean isAdjustingAngle,
            boolean isCheatMode,
            boolean isLocked

    ) {
        super(entityPos, p, i, d, v, t);
        this.offset = offset;
        this.isAdjustingAngle = isAdjustingAngle;
        this.isCheatMode =isCheatMode;
        this.isLocked = isLocked;
        // cycleMode.visible = true;
    }

    @Override
    public void initWidgets() {
        super.initWidgets();
        oField = new EditBox(font, 0, 0, common_input_width, common_input_height, Components.literal("offset"));
        oField.setTextColor(-1);
        oField.setTextColorUneditable(-1);
        oField.setBordered(true);

        Component mode = isAdjustingAngle ? ExposedFieldType.MODE_ANGULAR.getComponent() : ExposedFieldType.MODE_SPEED.getComponent();
        mLabel = new Label(0, 0, mode).colored(common_label_color);
        mLabel.text = mode;
        addRenderableWidget(mLabel);

        cLabel = new Label(0, 0, ExposedFieldType.MODE_CHEAT.getComponent());
        cLabel.text = ExposedFieldType.MODE_CHEAT.getComponent();
        addRenderableWidget(cLabel);

        lLabel = new Label(0, 0, ExposedFieldType.IS_LOCKED$1.getComponent()).colored((isLocked ? Color.RED : Color.GREEN).getRGB());
        lLabel.text = isLocked ?
                Component.translatable(ControlCraft.MODID + ".screen.labels.locked") :
                Component.translatable(ControlCraft.MODID + ".screen.labels.free");
        addRenderableWidget(lLabel);

        Component onOff = isCheatMode ? AllGuiLabels.onLabel : AllGuiLabels.offLabel;
        cField = new Label(0, 0, onOff).colored((isCheatMode ? Color.RED : Color.GRAY).getRGB());;
        cField.text = onOff;
        addRenderableWidget(cField);

        toggleCheatMode = new IconButton(0, 0, AllIcons.I_ARM_FORCED_ROUND_ROBIN);
        toggleCheatMode.withCallback(this::toggleCheatMode);
        toggleCheatMode.setToolTip(AllGuiLabels.cheatLabel);
        addRenderableWidget(toggleCheatMode);

        toggleReverse = new IconButton(0, 0, AllIcons.I_ARM_FORCED_ROUND_ROBIN);
        toggleReverse.withCallback(this::setToggleReverse);
        toggleReverse.setToolTip(Component.translatable(ControlCraft.MODID + ".tooltip.reverse"));
        addRenderableWidget(toggleReverse);

        toggleSoftLock = new IconButton(0, 0, AllIcons.I_CART_ROTATE_LOCKED);
        toggleSoftLock.withCallback(this::setToggleSoftLock);
        toggleSoftLock.setToolTip(Component.translatable(ControlCraft.MODID + ".tooltip.soft_lock"));
        addRenderableWidget(toggleSoftLock);


        oField.setMaxLength(35);
        oField.setValue(String.format("%.2f", offset));
        oField.setFilter(Util::tryParseDoubleFilter);

        addRenderableWidget(oField);

        oLabel = new Label(0, 0, ExposedFieldType.OFFSET.getComponent()).colored(common_label_color);
        oLabel.text = ExposedFieldType.OFFSET.getComponent();
        addRenderableWidget(oLabel);



    }

    @Override
    public void startWindow() {
        super.startWindow();
        controlValueLayout.addChild(oField, 4, 1);
        controlValueLayout.addChild(oLabel, 4, 0);
        statisticsLayout.addChild(mLabel, 1, 0);
        statisticsLayout.addChild(cLabel, 2, 0);
        statisticsLayout.addChild(cField, 2, 1);
        statisticsLayout.addChild(lLabel, 3, 0);
        controlValueLayout.rowSpacing(3);
        buttonLayout.addChild(toggleCheatMode, 0, 3);
        buttonLayout.addChild(toggleReverse, 0, 4);
        buttonLayout.addChild(toggleSoftLock, 0, 5);
        totalLayout.arrangeElements();
    }

    public void toggleCheatMode(){
        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.TOGGLE_0)
                .build();
        AllPackets.getChannel().sendToServer(p);
        super.register();
        onClose();
    }

    public void setToggleReverse(){
        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.TOGGLE_1)
                .build();
        AllPackets.getChannel().sendToServer(p);
        // super.register();
        onClose();
    }

    public void setToggleSoftLock(){
        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.TOGGLE_2)
                .build();
        AllPackets.getChannel().sendToServer(p);
        // super.register();
        onClose();
    }

    @Override
    public void register() {
        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.SETTING_0)
                .withDouble(Util.tryParseDouble(oField.getValue()))
                .build();
        AllPackets.getChannel().sendToServer(p);
        super.register();


    }

    @Override
    protected ItemStack renderedItem() {
        return AllBlocks.SERVO_MOTOR_BLOCK.asStack();
    }
}
