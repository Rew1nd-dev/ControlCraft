package com.verr1.controlcraft.content.gui.wand.mode;

import com.verr1.controlcraft.content.gui.wand.mode.base.WandAbstractDualSelectionMode;
import com.verr1.controlcraft.foundation.api.IBruteConnectable;
import com.verr1.controlcraft.foundation.data.WandSelection;
import com.verr1.controlcraft.foundation.managers.ClientOutliner;
import com.verr1.controlcraft.foundation.network.packets.GenericServerPacket;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.foundation.type.WandModesType;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.MinecraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;


@OnlyIn(value = Dist.CLIENT)
public class WandServoAssembleMode extends WandAbstractDualSelectionMode {
    public static final String ID = "servo_assemble";


    public static WandServoAssembleMode instance;


    public static void createInstance(){
        instance = new WandServoAssembleMode();
    }

    public WandServoAssembleMode getInstance(){
        return instance;
    }


    @Override
    public String getID() {
        return ID;
    }

    @Override
    public void onSelection(WandSelection selection) {
        if(state == State.TO_SELECT_X){
            BlockEntity be = Minecraft.getInstance().player.level().getExistingBlockEntity(selection.pos());
            if(!(be instanceof IBruteConnectable))return;
        }
        if(state == State.TO_SELECT_Y){
            if(selection.pos().equals(x.pos()))return;
        }
        super.onSelection(selection);
    }

    @Override
    public void tick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if(x == WandSelection.NULL)return;
        BlockEntity be = Minecraft.getInstance().player.level().getExistingBlockEntity(x.pos());
        if(!(be instanceof IBruteConnectable servo))return;

        Direction face = servo.getForward();

        ClientOutliner.drawOutline(x.pos(), face, Color.RED.getRGB(), "source");

        if(y != WandSelection.NULL) ClientOutliner.drawOutline(y.pos(), y.face(), Color.YELLOW.getRGB(), "target");
    }


    @Override
    public String tickCallBackInfo() {
        return WandModesType.SERVO.tickCallBackInfo(state).getString();
    }

    @Override
    protected void sendPacket(WandSelection x, WandSelection y) {

        if(x == WandSelection.NULL)return;
        BlockEntity be = Minecraft.getInstance().player.level().getExistingBlockEntity(x.pos());
        if(!(be instanceof IBruteConnectable servo))return;

        Direction face = servo.getForward();

        // for servo motor, align and forward is reversed here, for face alignment task to function properly
        // not a mistake

        var p = new GenericServerPacket.builder(RegisteredPacketType.CONNECT)
                .withLong(x.pos().asLong())

                .withLong(servo.getForward().ordinal())
                .withLong(servo.getAlign().ordinal())

                .withLong(y.pos().asLong())

                .withLong(MinecraftUtils.getVerticalDirectionSimple(y.face()).ordinal())
                .withLong(y.face().ordinal())

                .withLong(y.face().ordinal())
                .withLong(MinecraftUtils.getVerticalDirectionSimple(y.face()).ordinal())
                .build();

        ControlCraftPackets.getChannel().sendToServer(p);
    }

}
