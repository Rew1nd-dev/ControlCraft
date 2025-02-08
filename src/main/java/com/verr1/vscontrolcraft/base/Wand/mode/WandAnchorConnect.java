package com.verr1.vscontrolcraft.base.Wand.mode;

import com.verr1.vscontrolcraft.base.Wand.IWandMode;
import com.verr1.vscontrolcraft.base.Wand.WandSelection;
import com.verr1.vscontrolcraft.base.Wand.mode.base.WandAbstractDualSelectionMode;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialConnectPacket;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialAnchorBlockEntity;
import com.verr1.vscontrolcraft.registry.AllPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.awt.*;

public class WandAnchorConnect extends WandAbstractDualSelectionMode {
    public static final String ID = "anchor_connect";
    public static WandAnchorConnect instance;

    @Override
    public IWandMode getInstance() {
        return instance;
    }

    public static void createInstance(){
        instance = new WandAnchorConnect();
    }

    @Override
    public void onSelection(WandSelection selection) {
        if(state == State.TO_SELECT_X){
            BlockEntity be = Minecraft.getInstance().player.level().getExistingBlockEntity(selection.pos());
            if(!(be instanceof SpatialAnchorBlockEntity))return;
        }
        if(state == State.TO_SELECT_Y){
            if(y.pos() == x.pos())return;
            BlockEntity be = Minecraft.getInstance().player.level().getExistingBlockEntity(selection.pos());
            if(!(be instanceof SpatialAnchorBlockEntity))return;
        }
        super.onSelection(selection);
    }

    @Override
    public void tick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if(x == WandSelection.NULL)return;

        BlockEntity be = Minecraft.getInstance().player.level().getExistingBlockEntity(x.pos());
        if(!(be instanceof SpatialAnchorBlockEntity spatial_x))return;

        Direction xAlign = spatial_x.getAlign();
        Direction xForward = spatial_x.getForward();

        WandRenderer.drawOutline(x.pos(), xAlign, Color.RED.getRGB(), "source_0");
        WandRenderer.drawOutline(x.pos(), xForward, Color.GREEN.getRGB(), "source_1");



        if (y == WandSelection.NULL)return;

        BlockEntity be_ = Minecraft.getInstance().player.level().getExistingBlockEntity(y.pos());
        if(!(be_ instanceof SpatialAnchorBlockEntity spatial_y))return;

        Direction yAlign = spatial_y.getAlign();
        Direction yForward = spatial_y.getForward();

        WandRenderer.drawOutline(y.pos(), yAlign, Color.RED.getRGB(), "target_0");
        WandRenderer.drawOutline(y.pos(), yForward, Color.GREEN.getRGB(), "target_1");


    }

    @Override
    protected void sendPacket(WandSelection x, WandSelection y) {
        if(x == WandSelection.NULL)return;
        if(y == WandSelection.NULL)return;

        AllPackets
                .getChannel()
                .sendToServer(
                        new SpatialConnectPacket(
                                x.pos(),
                                y.pos()
                        )
                );
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
    public String getID() {
        return ID;
    }
}
