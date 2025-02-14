package com.verr1.vscontrolcraft.blocks.propeller;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.vscontrolcraft.base.ISyncable;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

import static net.minecraft.ChatFormatting.GRAY;

/*
* TODO:
*  1.   add some propeller classes that extends this class
*  2. √ make propeller stat changeable (GUI)
* */

public class PropellerBlockEntity extends SmartBlockEntity implements
        ISyncable, IHaveGoggleInformation, IPacketHandler
{
    public double ThrustRatio = 1000;
    public double TorqueRatio = 1000;
    public boolean ReverseTorque = false;
    public double rotationalSpeed = 5;

    public LerpedFloat angle;
    public float targetAngle = 0;



    public PropellerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        angle = LerpedFloat.angular();
        lazyTickRate = 3;
    }

    public double getThrustRatio() {
        return ThrustRatio;
    }

    public double getTorqueRatio(){
        return TorqueRatio;
    }

    public boolean getReverseTorque(){
        return ReverseTorque;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    public void setVisualRotationalSpeed(double speed){
        rotationalSpeed = speed;
    }

    public void setProperty(double torqueRatio, double thrustRatio, boolean reverseTorque){
        this.TorqueRatio = torqueRatio;
        this.ThrustRatio = thrustRatio;
        this.ReverseTorque = reverseTorque;
        sendData();
    }


    @Override
    public void tick() {
        super.tick();
        targetAngle = Util.angleReset(targetAngle + (float) (rotationalSpeed * 0.05 * 0.175));
        if (level.isClientSide) {
            tickAnimation();
        }
    }

    @Override
    public void lazyTick() {
        if(level.isClientSide)return;
        syncClient();
    }

    @OnlyIn(Dist.CLIENT)
    private void tickAnimation(){
        angle.chase(targetAngle, 0.1, LerpedFloat.Chaser.EXP);
        angle.tickChaser();
    }


    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        if(!clientPacket){
            tag.putDouble("ThrustRatio", ThrustRatio);
            tag.putDouble("TorqueRatio", TorqueRatio);
            tag.putBoolean("ReverseTorque", ReverseTorque);
        }
        super.write(tag, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        if(!clientPacket){
            ThrustRatio = tag.getDouble("ThrustRatio");
            TorqueRatio = tag.getDouble("TorqueRatio");
            ReverseTorque = tag.getBoolean("ReverseTorque");
        }
        super.read(tag, clientPacket);
    }

    @Override
    public void syncClient() {
        if(!level.isClientSide){
            var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.SYNC_0)
                    .withDouble(rotationalSpeed)
                    .build();
            AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.text("Propeller Statistic")
                .style(GRAY)
                .forGoggles(tooltip);

        float omega = (float)rotationalSpeed;

        Lang.number(omega)
                .text("/s")
                .style(ChatFormatting.AQUA)
                .space()
                .add(Lang.text("current omega")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
        return true;
    }


    public void displayScreen(ServerPlayer player){
        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.OPEN_SCREEN_0)
                .withDouble(TorqueRatio)
                .withDouble(ThrustRatio)
                .build();

        AllPackets.sendToPlayer(p, player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SYNC_0){
            double speed = packet.getDoubles().get(0);
            setVisualRotationalSpeed(speed);
        }
        if(packet.getType() == BlockBoundPacketType.OPEN_SCREEN_0){
            double torque_ratio = packet.getDoubles().get(0);
            double thrust_ratio = packet.getDoubles().get(1);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ScreenOpener.open(new PropellerScreen(packet.getBoundPos(), thrust_ratio, torque_ratio))
            );
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SETTING_0){
            double torque_ratio = packet.getDoubles().get(0);
            double thrust_ratio = packet.getDoubles().get(1);
            setProperty(torque_ratio, thrust_ratio, false);
        }
    }
}
