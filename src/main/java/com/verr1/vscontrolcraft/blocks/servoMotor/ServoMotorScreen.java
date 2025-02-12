package com.verr1.vscontrolcraft.blocks.servoMotor;

import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.utility.Components;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerScreen;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerType;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class ServoMotorScreen extends PIDControllerScreen {

    private final double offset;

    protected EditBox oField;

    protected Label oLabel;

    public ServoMotorScreen(BlockPos entityPos, double p, double i, double d, double v, double t, double offset) {
        super(entityPos, p, i, d, v, t);
        this.offset = offset;
        // cycleMode.visible = true;
    }

    @Override
    public void initWidgets() {
        super.initWidgets();
        oField = new EditBox(font, 0, 0, common_input_width, common_input_height, Components.literal("offset"));
        oField.setTextColor(-1);
        oField.setTextColorUneditable(-1);
        oField.setBordered(true);

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
        controlValueLayout.rowSpacing(3);
        totalLayout.arrangeElements();
    }


    @Override
    public void register() {
        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.SETTING)
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
