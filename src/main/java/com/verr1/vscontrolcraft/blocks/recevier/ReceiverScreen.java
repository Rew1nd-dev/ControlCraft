package com.verr1.vscontrolcraft.blocks.recevier;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.verr1.vscontrolcraft.base.SimpleSettingScreen;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
        addFieldWithLabel("Type: ", (s) -> true).setValue(receivedType);
        addFieldWithLabel("Name:", (s) -> true).setValue(receivedName);
        addFieldWithLabel("Channel:", Util::tryParseLongFilter).setValue(receivedProtocol + "");

        iFields.get(0).setEditable(false);
        iFields.get(0).setBordered(false);
    }


    @Override
    public void tick(){
        super.tick();
    }

    public void register() {

        AllPackets
                .getChannel()
                .sendToServer(
                        new ReceiverRegisterPacket(
                                pos,
                                iFields.get(1).getValue(),
                                Util.tryParseLong(iFields.get(2).getValue())
                        )
                );
        onClose();
    }

    @Override
    protected @Nullable ItemStack renderedStack() {
        return AllBlocks.RECEIVER_BLOCK.asStack();
    }
}
