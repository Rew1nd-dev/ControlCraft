package com.verr1.controlcraft.content.gui;

import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ReceiverScreen extends SimpleSettingScreen {


    private String receivedName;
    private String receivedType;
    private long receivedProtocol;
    private BlockPos pos;

    public ReceiverScreen(BlockPos entityPos, String name, String peripheralType, long protocol) {
        pos = entityPos;
        receivedName = name;
        receivedType = peripheralType;
        receivedProtocol = protocol;
    }


    @Override
    public void startWindow() {
        addNumericFieldWithLabel(ExposedFieldType.TYPE.getComponent(), (s) -> true).setValue(receivedType);
        addNumericFieldWithLabel(ExposedFieldType.NAME.getComponent(), (s) -> true).setValue(receivedName);
        addNumericFieldWithLabel(ExposedFieldType.PROTOCOL.getComponent(), ParseUtils::tryParseLongFilter).setValue(receivedProtocol + "");

        iFields.get(0).setEditable(false);
        iFields.get(0).setBordered(false);
    }

    @Override
    public void init() {
        super.init();
        redstoneSettings.visible = false;
    }

    @Override
    public void tick(){
        super.tick();
    }

    public void register() {
        var p = new BlockBoundServerPacket.builder(pos, RegisteredPacketType.SETTING_0)
                .withUtf8(iFields.get(1).getValue())
                .withLong(ParseUtils.tryParseLong(iFields.get(2).getValue()))
                .build();
        ControlCraftPackets
                .getChannel()
                .sendToServer(p);
        onClose();
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    protected @Nullable ItemStack renderedStack() {
        return ControlCraftBlocks.RECEIVER_BLOCK.asStack();
    }
}
