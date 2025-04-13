package com.verr1.controlcraft.content.blocks.propeller;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.network.executors.ClientBuffer;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.foundation.type.Side;
import com.verr1.controlcraft.foundation.api.IPacketHandler;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.MathUtils;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

public class PropellerBlockEntity extends OnShipBlockEntity implements
        IHaveGoggleInformation, IPacketHandler
{
    public static NetworkKey THRUST = NetworkKey.create("thrust");
    public static NetworkKey TORQUE = NetworkKey.create("torque");
    public static NetworkKey SPEED = NetworkKey.create("speed");

    public double ThrustRatio = 1000;
    public double TorqueRatio = 1000;

    public double getRotationalSpeed() {
        return rotationalSpeed;
    }

    public double rotationalSpeed = 5;

    public LerpedFloat angle;
    public float targetAngle = 0;


    public double getThrustRatio() {
        return ThrustRatio;
    }

    public double getTorqueRatio(){
        return TorqueRatio;
    }



    public void setVisualRotationalSpeed(double speed){
        rotationalSpeed = speed;
    }


    public void setThrustRatio(double thrustRatio) {
        ThrustRatio = thrustRatio;
    }

    public void setTorqueRatio(double torqueRatio) {
        TorqueRatio = torqueRatio;
    }

    public void setProperty(double torqueRatio, double thrustRatio){
        this.TorqueRatio = torqueRatio;
        this.ThrustRatio = thrustRatio;
        setChanged();
    }


    @Override
    public void tickCommon() {
        super.tickCommon();
        targetAngle = MathUtils.angleReset(targetAngle + (float) (rotationalSpeed * 0.05 * 0.175));
    }

    @Override
    public void tickClient() {
        super.tickClient();
        tickAnimation();
    }

    @Override
    public void lazyTickServer() {
        syncForNear(true, SPEED);
    }

    @OnlyIn(Dist.CLIENT)
    private void tickAnimation(){
        angle.chase(targetAngle, 0.1, LerpedFloat.Chaser.EXP);
        angle.tickChaser();
    }

    public PropellerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        buildRegistry(THRUST).withBasic(SerializePort.of(this::getThrustRatio, this::setThrustRatio, SerializeUtils.DOUBLE)).withClient(ClientBuffer.DOUBLE.get()).register();
        buildRegistry(TORQUE).withBasic(SerializePort.of(this::getTorqueRatio, this::setTorqueRatio, SerializeUtils.DOUBLE)).withClient(ClientBuffer.DOUBLE.get()).register();
        buildRegistry(SPEED).withBasic(SerializePort.of(this::getRotationalSpeed, this::setVisualRotationalSpeed, SerializeUtils.DOUBLE)).withClient(ClientBuffer.DOUBLE.get()).dispatchToSync().register();
        /*
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::getThrustRatio, this::setThrustRatio, SerializeUtils.DOUBLE, THRUST), Side.SHARED);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::getTorqueRatio, this::setTorqueRatio, SerializeUtils.DOUBLE, TORQUE), Side.SHARED);
        registerFieldReadWriter(SerializeUtils.ReadWriter.of(this::getRotationalSpeed, this::setVisualRotationalSpeed, SerializeUtils.DOUBLE, SPEED), Side.SHARED);
        * */
        angle = LerpedFloat.angular();
        lazyTickRate = 3;
    }


    public void syncClient() {
        if(level == null || level.isClientSide)return;
        var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.SYNC_0)
                .withDouble(rotationalSpeed)
                .build();
        ControlCraftPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
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
        var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.OPEN_SCREEN_0)
                .withDouble(TorqueRatio)
                .withDouble(ThrustRatio)
                .build();

        ControlCraftPackets.sendToPlayer(p, player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == RegisteredPacketType.SYNC_0){
            double speed = packet.getDoubles().get(0);
            setVisualRotationalSpeed(speed);
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {
        if(packet.getType() == RegisteredPacketType.SETTING_0){
            double thrust_ratio = packet.getDoubles().get(0);
            double torque_ratio = packet.getDoubles().get(1);
            setProperty(torque_ratio, thrust_ratio);
        }
    }
}
