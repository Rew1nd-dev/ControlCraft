package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.base.Servo.ICanBruteDirectionalConnect;
import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.mode.base.WandAbstractTripleSelectionMode;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.blocks.revoluteJoint.DirectionalAssemblePacket;
import com.verr1.vscontrolcraft.blocks.slider.SliderControllerBlock;
import com.verr1.vscontrolcraft.blocks.slider.SliderControllerBlockEntity;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.awt.*;

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
            if(!(be instanceof SliderControllerBlockEntity))return;
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
        BlockEntity be = Minecraft.getInstance().player.level().getExistingBlockEntity(x.pos());
        if(!(be instanceof SliderControllerBlockEntity joint))return;

        BlockPos servoPos = x.pos();
        BlockPos assemPos = y.pos();
        Direction assemAlign = y.face();
        Direction assemForward = z.face();



        Direction align = joint.getAlign();
        Direction forward = joint.getForward();

        AllPackets
                .getChannel()
                .sendToServer(
                        new DirectionalAssemblePacket(
                                assemPos,
                                servoPos,
                                assemAlign,
                                align,
                                assemForward,
                                forward
                )
            );
    }

    @Override
    public void tick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if(x == WandSelection.NULL)return;
        BlockEntity be = Minecraft.getInstance().player.level().getExistingBlockEntity(x.pos());
        if(!(be instanceof SliderControllerBlockEntity joint))return;

        Direction align = joint.getAlign();
        Direction forward = joint.getForward();



        WandRenderer.drawOutline(x.pos(), align, Color.RED.getRGB(), "x1");
        WandRenderer.drawOutline(x.pos(), forward, Color.YELLOW.getRGB(), "x2");
        if(y != WandSelection.NULL) WandRenderer.drawOutline(y.pos(), y.face(), Color.RED.getRGB(), "y");
        if(z != WandSelection.NULL) WandRenderer.drawOutline(z.pos(), z.face(), Color.YELLOW.getRGB(), "z");
    }


    protected void lazyTick(){
        WandRenderer.textPlayerWhenHoldingWand(tickCallBackInfo());
    }

    @Override
    public String tickCallBackInfo() {
        if(state == State.TO_SELECT_X){
            return "please select joint motor";
        }
        if(state == State.TO_SELECT_Y){
            return "select the face you want to face towards motor face (RED)";
        }
        if(state == State.TO_SELECT_Z){
            return "select the face whose direction is to set parallel with rotation direction (YELLOW)";
        }
        if(state == State.TO_CONFIRM){
            return "right click to confirm assembly";
        }
        return "";
    }

    @Override
    public String getID() {
        return ID;
    }
}
