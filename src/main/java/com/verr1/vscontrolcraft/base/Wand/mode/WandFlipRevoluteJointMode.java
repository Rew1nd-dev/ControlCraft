package com.verr1.vscontrolcraft.base.Wand.mode;

import com.jozufozu.flywheel.util.Color;
import com.verr1.vscontrolcraft.base.Hinge.packets.HingeFlipPacket;
import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.mode.base.WandAbstractMultipleSelectionMode;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.blocks.revoluteJoint.RevoluteJointBlockEntity;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(value = Dist.CLIENT)
public class WandFlipRevoluteJointMode extends WandAbstractMultipleSelectionMode {
    public static final String ID = "flip_revolute_joint";

    public static WandFlipRevoluteJointMode instance;

    public static void createInstance(){
        instance = new WandFlipRevoluteJointMode();
    }

    private WandFlipRevoluteJointMode(){

    }

    @Override
    public IWandMode getInstance() {
        return instance;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public void onSelection(WandSelection selection) {
        AllPackets
                .getChannel()
                .sendToServer(new HingeFlipPacket(selection.pos()));
    }


    @Override
    protected void tick() {
        if(WandRenderer.lookingAt() instanceof RevoluteJointBlockEntity rvl){
            WandRenderer.drawOutline(rvl.getBlockPos(), rvl.getJointDirection(), Color.RED.getRGB(), "rvl_joint_dir");
        }
    }

    protected void lazyTick(){
        WandRenderer.textPlayerWhenHoldingWand(tickCallBackInfo());
    }

    @Override
    public String tickCallBackInfo() {
        return "right click hinge to adjust level";
    }
}
