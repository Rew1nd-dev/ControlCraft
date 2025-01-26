package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.base.Hinge.packets.HingeBruteConnectPacket;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.mode.base.WandAbstractDualSelectionMode;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.blocks.revoluteJoint.RevoluteJointBlockEntity;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WandRevoluteJointConnectionMode extends WandAbstractDualSelectionMode {
    public static final String ID = "hinge_connection";

    public static WandRevoluteJointConnectionMode instance;


    public static void createInstance(){
        instance = new WandRevoluteJointConnectionMode();
    }

    public WandRevoluteJointConnectionMode getInstance(){
        return instance;
    }

    private WandRevoluteJointConnectionMode(){
        super();
    }

    @Override
    public String getID() {
        return ID;
    }

    protected void lazyTick(){
        WandRenderer.textPlayerWhenHoldingWand(tickCallBackInfo());
    }

    @Override
    public String tickCallBackInfo() {
        if(state == State.TO_SELECT_X){
            return "please select Revolute Hinge X";
        }
        if(state == State.TO_SELECT_Y){
            return "please select Revolute Hinge Y, Click The Face To Set Parallel With X (RED)";
        }
        if(state == State.TO_CONFIRM){
            return "right click to confirm assembly";
        }
        return "";
    }

    @Override
    public void onSelection(WandSelection selection) {
        if(state == State.TO_SELECT_X){
            if(EntityOrNull(x) == null)return;
        }
        if(state == State.TO_SELECT_Y){
            RevoluteJointBlockEntity rvl_y = EntityOrNull(y);
            RevoluteJointBlockEntity rvl_x = EntityOrNull(x);
            if(rvl_y == null || rvl_x == null)return;
            if(selection.pos().equals(x.pos()))return;

            if(selection.face() != rvl_y.getJointDirection() || selection.face() != rvl_y.getJointDirection().getOpposite())return;


        }
        super.onSelection(selection);
    }

    private RevoluteJointBlockEntity EntityOrNull(WandSelection selection){
        if(!isValid(selection))return null;
        BlockEntity be = Minecraft.getInstance().player.level().getExistingBlockEntity(selection.pos());
        if(!(be instanceof RevoluteJointBlockEntity rvl))return null;
        return rvl;
    }


    @Override
    public void tick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if(!isValid(x))return;

        RevoluteJointBlockEntity rvl_y = EntityOrNull(y);
        RevoluteJointBlockEntity rvl_x = EntityOrNull(x);
        if(rvl_y == null || rvl_x == null)return;



        WandRenderer.drawOutline(x.pos(), rvl_x.getJointDirection().getOpposite(), 0xaaca32, "source");
        if(isValid(y)) WandRenderer.drawOutline(y.pos(), rvl_y.getJointDirection(), 0xffcb74, "target");
    }


    @Override
    protected void sendPacket(WandSelection x, WandSelection y) {
        AllPackets.getChannel().sendToServer(new HingeBruteConnectPacket(y.pos(), x.pos())); // x.bruteConnect(y)
    }

}
