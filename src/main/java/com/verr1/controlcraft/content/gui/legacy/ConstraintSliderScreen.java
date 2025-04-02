package com.verr1.controlcraft.content.gui.legacy;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import com.verr1.controlcraft.registry.ControlCraftGuiLabels;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ConstraintSliderScreen extends SimpleSettingScreen{
    private final double value;
    private final double lerpSpeed;
    private final double target;
    private final double compliance;

    public ConstraintSliderScreen(BlockPos pos, double value, double target, double compliance, double lerpSpeed) {
        super(pos);
        this.value = value;
        this.target = target;
        this.lerpSpeed = lerpSpeed;
        this.compliance = compliance;
    }


    @Override
    public void startWindow() {
        addNumericFieldWithLabel(ExposedFieldType.VALUE.asComponent(), ParseUtils::tryParseDoubleFilter, false)
                .setValue(String.format("%.2f", value));
        addNumericFieldWithLabel(ExposedFieldType.FORCED_TARGET.asComponent(), ParseUtils::tryParseDoubleFilter, true)
                .setValue(String.format("%.2f", target));
        addNumericFieldWithLabel(ExposedFieldType.SPEED.asComponent(), ParseUtils::tryParseDoubleFilter, true)
                .setValue(String.format("%.2f", lerpSpeed));
        addNumericFieldWithLabel(ExposedFieldType.COMPLIANCE.asComponent(), ParseUtils::tryParseDoubleFilter, true)
                .setValue(String.format("%.2f", compliance));
    }

    @Override
    public void init() {
        super.init();
        IconButton cycleMode = new IconButton(0, 0, AllIcons.I_ACTIVE);
        cycleMode.withCallback(this::cycleMode);

        cycleMode.setToolTip(ControlCraftGuiLabels.cycleLabel);
        cycleMode.setY(guiTop + windowHeight - 12 - 22);
        cycleMode.setX(guiLeft + 4 + 20 + 20);

        addRenderableWidget(cycleMode);
    }

    public void cycleMode(){
        var p = new BlockBoundServerPacket.builder(getPos(), RegisteredPacketType.TOGGLE_0)
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
        register();
    }

    @Override
    public void register() {
        var p = new BlockBoundServerPacket.builder(getPos(), RegisteredPacketType.SETTING_1)
                .withDouble(ParseUtils.tryParseDouble(iFields.get(1).getValue()))
                .withDouble(ParseUtils.tryParseDouble(iFields.get(2).getValue()))
                .withDouble(ParseUtils.tryParseDouble(iFields.get(3).getValue()))
                .build();
        ControlCraftPackets.getChannel().sendToServer(p);
        onClose();
    }



    @Override
    protected @Nullable ItemStack renderedStack() {
        return ControlCraftBlocks.CONSTRAINT_SLIDER_BLOCK.asStack();
    }
}
