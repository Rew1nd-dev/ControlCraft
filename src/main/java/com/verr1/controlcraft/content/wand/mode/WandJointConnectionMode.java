package com.verr1.controlcraft.content.wand.mode;


import com.jozufozu.flywheel.util.Color;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.content.blocks.joints.RevoluteJointBlockEntity;
import com.verr1.controlcraft.content.wand.WandGUI;
import com.verr1.controlcraft.content.wand.mode.base.WandAbstractDualSelectionMode;
import com.verr1.controlcraft.foundation.api.IBruteConnectable;
import com.verr1.controlcraft.foundation.data.WandSelection;
import com.verr1.controlcraft.foundation.managers.ClientOutliner;
import com.verr1.controlcraft.foundation.network.packets.GenericServerPacket;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.MinecraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(value = Dist.CLIENT)
public class WandJointConnectionMode extends WandAbstractDualSelectionMode {
    public static final String ID = "hinge_connection";

    public static WandJointConnectionMode instance;


    public static void createInstance(){
        instance = new WandJointConnectionMode();
    }

    public WandJointConnectionMode getInstance(){
        return instance;
    }

    private WandJointConnectionMode(){
        super();
    }

    @Override
    public String getID() {
        return ID;
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
        if(!WandGUI.isClientWandInHand() || !WandGUI.isWrenchInHand())return;
        if(MinecraftUtils.lookingAt() instanceof RevoluteJointBlockEntity rvl){
            ClientOutliner.drawOutline(rvl.getBlockPos(), rvl.getJointDirection(), Color.RED.getRGB(), "rvl_joint_dir");
        }
        super.tick();

    }

    @Override
    protected void sendPacket(WandSelection x, WandSelection y) {
        BlockPos jointPos = x.pos();
        BlockPos compPos = y.pos();

        Optional
            .ofNullable(Minecraft.getInstance().player)
            .map(Player::level)
            .map(level -> level.getBlockEntity(jointPos))
            .filter(be -> be instanceof IBruteConnectable)
            .map(IBruteConnectable.class::cast)
            .ifPresent(
                joint -> {
                    var p = new GenericServerPacket.builder(RegisteredPacketType.BRUTE_CONNECT)
                            .withLong(jointPos.asLong())
                            .withLong(Direction.UP.ordinal()) // this is simply occupying the packet index
                            .withLong(MinecraftUtils.getVerticalDirectionSimple(Direction.UP).ordinal())
                            .withLong(compPos.asLong())
                            .withLong(Direction.UP.ordinal())
                            .withLong(MinecraftUtils.getVerticalDirectionSimple(Direction.UP).ordinal())
                            .build();

                    ControlCraftPackets.getChannel().sendToServer(p);
                });
    }


}
