package com.verr1.controlcraft.content.gui.wand.mode;

import com.verr1.controlcraft.content.blocks.slider.DynamicSliderBlockEntity;
import com.verr1.controlcraft.content.gui.wand.mode.base.WandAbstractTripleSelectionMode;
import com.verr1.controlcraft.foundation.api.IBruteConnectable;
import com.verr1.controlcraft.foundation.api.IWandMode;
import com.verr1.controlcraft.foundation.data.WandSelection;
import com.verr1.controlcraft.foundation.managers.ClientOutliner;
import com.verr1.controlcraft.foundation.network.packets.GenericServerPacket;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.foundation.type.WandModesType;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.Optional;

@OnlyIn(value = Dist.CLIENT)
public class WandSliderAssembleMode extends WandAbstractTripleSelectionMode {
    public static final String ID = "slider_assemble";
    public static WandSliderAssembleMode instance;
    // x is the joint pos,
    // y selects the face player want to face towards motor block direction,
    // z selects the face to set along with rotation direction

    @Override
    public IWandMode getInstance() {
        return instance;
    }

    public static void createInstance(){
        instance = new WandSliderAssembleMode();
    }

    @Override
    public void onSelection(WandSelection selection) {
        if(state == State.TO_SELECT_X){
            BlockEntity be = Minecraft.getInstance().player.level().getExistingBlockEntity(selection.pos());
            if(!(be instanceof DynamicSliderBlockEntity))return;
        }
        if(state == State.TO_SELECT_Y){
            if(selection.pos().equals(x.pos()))return;
        }
        if(state == State.TO_SELECT_Z){
            if(selection.face() == y.face() || selection.face() == y.face().getOpposite())return;
        }
        super.onSelection(selection);
    }



    @Override
    protected void sendPacket(WandSelection x, WandSelection y, WandSelection z) {
        BlockPos sliderPos = x.pos();
        BlockPos compPos = y.pos();
        Direction compAlign = y.face();
        Direction compForward = z.face();


        Optional.ofNullable(Minecraft.getInstance().player)
                .map(Player::level)
                .map(level -> level.getBlockEntity(sliderPos))
                .filter(be -> be instanceof IBruteConnectable)
                .map(IBruteConnectable.class::cast)
                .ifPresent(
                        IBC -> {
                            Direction sliderAlign = IBC.getAlign();
                            Direction sliderForward = IBC.getForward();

                            var p = new GenericServerPacket.builder(RegisteredPacketType.CONNECT)
                                    .withLong(sliderPos.asLong())
                                    .withLong(sliderAlign.ordinal())
                                    .withLong(sliderForward.ordinal())
                                    .withLong(compPos.asLong())
                                    .withLong(compAlign.ordinal())
                                    .withLong(compForward.ordinal())

                                    .withLong(compAlign.ordinal())
                                    .withLong(compForward.ordinal())

                                    .build();

                            ControlCraftPackets.getChannel().sendToServer(p);
                        });
    }

    @Override
    public void tick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if(x == WandSelection.NULL)return;
        BlockEntity be = Minecraft.getInstance().player.level().getExistingBlockEntity(x.pos());
        if(!(be instanceof DynamicSliderBlockEntity joint))return;

        Direction align = joint.getAlign();
        Direction forward = joint.getForward();



        ClientOutliner.drawOutline(x.pos(), align, Color.RED.getRGB(), "x1");
        ClientOutliner.drawOutline(x.pos(), forward, Color.YELLOW.getRGB(), "x2");
        if(y != WandSelection.NULL) ClientOutliner.drawOutline(y.pos(), y.face(), Color.RED.getRGB(), "y");
        if(z != WandSelection.NULL) ClientOutliner.drawOutline(z.pos(), z.face(), Color.YELLOW.getRGB(), "z");
    }



    @Override
    public String tickCallBackInfo() {
        return WandModesType.SLIDER.tickCallBackInfo(state).getString();
    }

    @Override
    public String getID() {
        return ID;
    }
}
