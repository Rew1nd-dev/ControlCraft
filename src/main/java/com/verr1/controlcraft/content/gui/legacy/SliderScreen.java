package com.verr1.controlcraft.content.gui.legacy;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import com.verr1.controlcraft.registry.ControlCraftGuiLabels;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class SliderScreen extends ControllerScreen {

    private final boolean isCheatMode;
    private final boolean isLocked;
    private final boolean isAdjustingPosition;




    protected Label mLabel;

    protected Label cLabel;
    protected Label cField;

    protected Label lLabel;

    protected IconButton toggleCheatMode;
    protected IconButton toggleSoftLock;

    public SliderScreen(BlockPos entityPos, double p, double i, double d, double v, double t, boolean m, boolean l, boolean c) {
        super(entityPos, p, i, d, v, t);
        this.isLocked= l;
        this.isAdjustingPosition = m;
        this.isCheatMode = c;
    }

    @Override
    public void initWidgets() {
        super.initWidgets();

        Component mode = isAdjustingPosition ? ExposedFieldType.MODE_POSITION.asComponent() : ExposedFieldType.MODE_SPEED.asComponent();
        mLabel = new Label(0, 0, mode).colored(common_label_color);
        mLabel.text = mode;
        addRenderableWidget(mLabel);

        cLabel = new Label(0, 0, ExposedFieldType.MODE_CHEAT.asComponent());
        cLabel.text = ExposedFieldType.MODE_CHEAT.asComponent();
        addRenderableWidget(cLabel);

        lLabel = new Label(0, 0, ExposedFieldType.IS_LOCKED$1.asComponent()).colored((isLocked ? Color.RED : Color.GREEN).getRGB());
        lLabel.text = isLocked ?
                Component.translatable(ControlCraft.MODID + ".screen.labels.locked") :
                Component.translatable(ControlCraft.MODID + ".screen.labels.free");
        addRenderableWidget(lLabel);

        Component onOff = isCheatMode ? ControlCraftGuiLabels.onLabel : ControlCraftGuiLabels.offLabel;
        cField = new Label(0, 0, onOff).colored((isCheatMode ? Color.RED : Color.GRAY).getRGB());;
        cField.text = onOff;
        addRenderableWidget(cField);

        toggleCheatMode = new IconButton(0, 0, AllIcons.I_ARM_FORCED_ROUND_ROBIN);
        toggleCheatMode.withCallback(this::toggleCheatMode);
        toggleCheatMode.setToolTip(ControlCraftGuiLabels.cheatLabel);
        addRenderableWidget(toggleCheatMode);

        toggleSoftLock = new IconButton(0, 0, AllIcons.I_CART_ROTATE_LOCKED);
        toggleSoftLock.withCallback(this::setToggleSoftLock);
        toggleSoftLock.setToolTip(Component.translatable(ControlCraft.MODID + ".tooltip.soft_lock"));
        addRenderableWidget(toggleSoftLock);


    }

    @Override
    public void startWindow() {
        super.startWindow();
        statisticsLayout.addChild(mLabel, 1, 0);
        statisticsLayout.addChild(cLabel, 2, 0);
        statisticsLayout.addChild(cField, 2, 1);
        statisticsLayout.addChild(lLabel, 3, 0);
        controlValueLayout.rowSpacing(3);
        buttonLayout.addChild(toggleCheatMode, 0, 3);
        buttonLayout.addChild(toggleSoftLock, 0, 4);
        totalLayout.arrangeElements();
    }

    public void toggleCheatMode(){
        var p = new BlockBoundServerPacket.builder(pos, RegisteredPacketType.TOGGLE_0)
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
        super.register();
        onClose();
    }

    public void setToggleSoftLock(){
        var p = new BlockBoundServerPacket.builder(pos, RegisteredPacketType.TOGGLE_1)
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
        // super.register();
        onClose();
    }

    @Override
    protected ItemStack renderedItem() {
        return ControlCraftBlocks.SLIDER_CONTROLLER_BLOCK.asStack();
    }
}
