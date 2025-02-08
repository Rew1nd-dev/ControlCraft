package com.verr1.vscontrolcraft.base.Wand.mode;

import com.jozufozu.flywheel.util.Color;
import com.verr1.vscontrolcraft.base.Hinge.packets.HingeBruteConnectPacket;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.mode.base.WandAbstractDualSelectionMode;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.blocks.revoluteJoint.RevoluteJointBlockEntity;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(value = Dist.CLIENT)
public class WandHingeConnectionMode extends WandAbstractDualSelectionMode {
    public static final String ID = "hinge_connection";

    public static WandHingeConnectionMode instance;


    public static void createInstance(){
        instance = new WandHingeConnectionMode();
    }

    public WandHingeConnectionMode getInstance(){
        return instance;
    }

    private WandHingeConnectionMode(){
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
            return "please select Hinge X";
        }
        if(state == State.TO_SELECT_Y){
            return "please select Hinge Y";
        }
        if(state == State.TO_CONFIRM){
            return "right click to confirm assembly";
        }
        return "";
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

        if(WandRenderer.lookingAt() instanceof RevoluteJointBlockEntity rvl){
            WandRenderer.drawOutline(rvl.getBlockPos(), rvl.getJointDirection(), Color.RED.getRGB(), "rvl_joint_dir");
        }
        super.tick();

    }

    @Override
    protected void sendPacket(WandSelection x, WandSelection y) {
        AllPackets.getChannel().sendToServer(new HingeBruteConnectPacket(y.pos(), x.pos())); // x.bruteConnect(y)
    }


}
