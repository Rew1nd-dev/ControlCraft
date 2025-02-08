package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.base.Wand.mode.base.WandAbstractDualSelectionMode;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.blocks.servoMotor.ServoMotorBlockEntity;
import com.verr1.vscontrolcraft.blocks.servoMotor.SimpleAssemblePacket;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.registry.AllPackets;
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
            if(!(be instanceof ServoMotorBlockEntity))return;
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
        if(!(be instanceof ServoMotorBlockEntity servo))return;

        Direction face = servo.getServoDirection();

        WandRenderer.drawOutline(x.pos(), face, Color.RED.getRGB(), "source");

        if(y != WandSelection.NULL) WandRenderer.drawOutline(y.pos(), y.face(), Color.YELLOW.getRGB(), "target");
    }

    protected void lazyTick(){
        WandRenderer.textPlayerWhenHoldingWand(tickCallBackInfo());
    }

    @Override
    public String tickCallBackInfo() {
        if(state == State.TO_SELECT_X){
            return "please select servo motor";
        }
        if(state == State.TO_SELECT_Y){
            return "select the face you want to face towards motor face (RED)";
        }
        if(state == State.TO_CONFIRM){
            return "right click to confirm assembly";
        }
        return "";
    }

    @Override
    protected void sendPacket(WandSelection x, WandSelection y) {
        AllPackets
                .getChannel()
                .sendToServer(
                        new SimpleAssemblePacket(
                                y.pos(),
                                x.pos(),
                                y.face(),
                                x.face()
                        )
                );
    }

}
