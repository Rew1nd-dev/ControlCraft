package com.verr1.controlcraft.content.blocks.motor;

import com.simibubi.create.foundation.gui.ScreenOpener;
import com.verr1.controlcraft.content.gui.RevoluteMotorScreen;
import com.verr1.controlcraft.foundation.data.control.PID;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.type.CheatMode;
import com.verr1.controlcraft.foundation.type.RegisteredPacketType;
import com.verr1.controlcraft.foundation.type.TargetMode;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.joml.Vector3d;
import org.valkyrienskies.mod.api.ValkyrienSkies;

public class RevoluteMotorBlockEntity extends AbstractMotorBlockEntity{
    public RevoluteMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Direction getServoDirection() {
        return getBlockState().getValue(RevoluteMotorBlock.FACING);
    }

    @Override
    public BlockPos getAssembleBlockPos() {
        return getBlockPos().relative(getBlockState().getValue(RevoluteMotorBlock.FACING));
    }

    @Override
    public Vector3d getRotationCenterPosJOML() {
        Vector3d center = ValkyrienSkies.set(new Vector3d(), getAssembleBlockPos().getCenter());
        Vector3d dir = getServoDirectionJOML();
        return center.fma(getOffset(), dir);
    }


    public void displayScreen(ServerPlayer player){

        double t = getController().getTarget();
        double v = getController().getValue();
        double o = getOffset();
        boolean m = getTargetMode() == TargetMode.POSITION;
        boolean c = getCheatMode() == CheatMode.NO_REPULSE;
        boolean l = isLocked();
        PID pidParams = getController().getPIDParams();

        var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.OPEN_SCREEN_0)
                .withDouble(t)
                .withDouble(v)
                .withDouble(pidParams.p())
                .withDouble(pidParams.i())
                .withDouble(pidParams.d())
                .withDouble(o)
                .withBoolean(m)
                .withBoolean(c)
                .withBoolean(l)
                .build();

        ControlCraftPackets.sendToPlayer(p, player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        super.handleClient(context, packet);
        if(packet.getType() == RegisteredPacketType.OPEN_SCREEN_0){
            double t = packet.getDoubles().get(0);
            double v = packet.getDoubles().get(1);
            double p = packet.getDoubles().get(2);
            double i = packet.getDoubles().get(3);
            double d = packet.getDoubles().get(4);
            double o = packet.getDoubles().get(5);
            boolean m = packet.getBooleans().get(0);
            boolean c = packet.getBooleans().get(1);
            boolean l = packet.getBooleans().get(2);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ScreenOpener.open(new RevoluteMotorScreen(getBlockPos(), p, i, d, v, t, o, m, c, l)));
        }
    }

}
