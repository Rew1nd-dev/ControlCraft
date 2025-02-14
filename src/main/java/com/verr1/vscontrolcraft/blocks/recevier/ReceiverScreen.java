package com.verr1.vscontrolcraft.blocks.recevier;

import com.verr1.vscontrolcraft.base.SimpleSettingScreen;
import com.verr1.vscontrolcraft.base.UltraTerminal.ExposedFieldType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
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
        addNumericFieldWithLabel(ExposedFieldType.PROTOCOL.getComponent(), Util::tryParseLongFilter).setValue(receivedProtocol + "");

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
        var p = new BlockBoundServerPacket.builder(pos, BlockBoundPacketType.SETTING_0)
                .withUtf8(iFields.get(1).getValue())
                .withLong(Util.tryParseLong(iFields.get(2).getValue()))
                .build();
        AllPackets
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
        return AllBlocks.RECEIVER_BLOCK.asStack();
    }
}
