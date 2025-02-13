package com.verr1.vscontrolcraft.blocks.jointMotor;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.utility.Components;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerScreen;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllGuiLabels;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class JointMotorScreen extends PIDControllerScreen {


    private final boolean isAdjustingAngle;

    protected Label mLabel;

    private final boolean isCheatMode;
    protected Label cLabel;
    protected Label cField;
    protected IconButton toggleCheatMode;

    public JointMotorScreen(
            BlockPos entityPos,
            double p,
            double i,
            double d,
            double v,
            double t,
            boolean isAdjustingAngle,
            boolean isCheatMode
    ) {
        super(entityPos, p, i, d, v, t);
        this.isAdjustingAngle = isAdjustingAngle;
        this.isCheatMode = isCheatMode;
    }

    @Override
    public void initWidgets() {
        super.initWidgets();


        Component mode = isAdjustingAngle ? ExposedFieldType.MODE_ANGULAR.getComponent() : ExposedFieldType.MODE_SPEED.getComponent();
        mLabel = new Label(0, 0, mode).colored(common_label_color);
        mLabel.text = mode;
        addRenderableWidget(mLabel);

        cLabel = new Label(0, 0, ExposedFieldType.MODE_CHEAT.getComponent());
        cLabel.text = ExposedFieldType.MODE_CHEAT.getComponent();
        addRenderableWidget(cLabel);

        toggleCheatMode = new IconButton(0, 0, AllIcons.I_ARM_FORCED_ROUND_ROBIN);
        toggleCheatMode.withCallback(this::toggleCheatMode);
        addRenderableWidget(toggleCheatMode);

        Component onOff= isCheatMode ? AllGuiLabels.onLabel : AllGuiLabels.offLabel;
        cField = new Label(0, 0, onOff);
        cField.text = onOff;
        addRenderableWidget(cField);

    }

    public void toggleCheatMode(){
        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.TOGGLE)
                .build();
        AllPackets.getChannel().sendToServer(p);
        super.register();
        onClose();
    }

    @Override
    public void startWindow() {
        super.startWindow();
        statisticsLayout.addChild(mLabel, 1, 0);
        statisticsLayout.addChild(cLabel, 2, 0);
        statisticsLayout.addChild(cField, 2, 1);
        buttonLayout.addChild(toggleCheatMode, 0, 3);
        controlValueLayout.rowSpacing(3);
        totalLayout.arrangeElements();
    }

    @Override
    protected ItemStack renderedItem() {
        return AllBlocks.JOINT_MOTOR_BLOCK.asStack();
    }
}
