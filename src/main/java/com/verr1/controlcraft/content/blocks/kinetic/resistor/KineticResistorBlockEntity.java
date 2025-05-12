package com.verr1.controlcraft.content.blocks.kinetic.resistor;

import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.simibubi.create.foundation.utility.Couple;
import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.foundation.api.delegate.INetworkHandle;
import com.verr1.controlcraft.foundation.api.delegate.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.data.NumericField;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.network.executors.ClientBuffer;
import com.verr1.controlcraft.foundation.network.executors.CompoundTagPort;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.foundation.network.handler.NetworkHandler;
import com.verr1.controlcraft.foundation.redstone.DirectReceiver;
import com.verr1.controlcraft.foundation.redstone.IReceiver;
import com.verr1.controlcraft.foundation.type.descriptive.SlotType;
import com.verr1.controlcraft.utils.MathUtils;
import com.verr1.controlcraft.utils.SerializeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class KineticResistorBlockEntity extends SplitShaftBlockEntity implements
        IReceiver, INetworkHandle
{
    public static final NetworkKey RATIO = NetworkKey.create("ratio");

    private double ratio = 1.0;

    private final DirectReceiver receiver = new DirectReceiver();

    private final NetworkHandler handler = new NetworkHandler(this);

    @Override
    public DirectReceiver receiver() {
        return receiver;
    }

    public KineticResistorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);

        handler
                .buildRegistry(FIELD_)
                .withBasic(CompoundTagPort.of(
                        () -> receiver().serialize(),
                        t -> receiver().deserialize(t)
                ))
                .withClient(
                        new ClientBuffer<>(SerializeUtils.UNIT, CompoundTag.class)
                )
                .dispatchToSync()
                .register();

        handler
                .buildRegistry(RATIO)
                .withBasic(SerializePort.of(
                        this::ratio,
                        t -> {
                            setRatioOnly(t);
                            ControlCraftServer.SERVER_EXECUTOR.executeLater(this::refreshKinetics, 2);
                        },
                        SerializeUtils.DOUBLE
                ))
                .withClient(
                        ClientBuffer.DOUBLE.get()
                ).register();

        receiver().register(
                new NumericField(
                        this::ratio,
                        this::setRatio,
                        "Ratio"
                ),
                new DirectReceiver.InitContext(SlotType.RATIO, Couple.create(0.0, 1.0)),
                new DirectReceiver.InitContext(SlotType.RATIO, Couple.create(0.0, 1.0)),
                new DirectReceiver.InitContext(SlotType.RATIO, Couple.create(0.0, 1.0)),
                new DirectReceiver.InitContext(SlotType.RATIO, Couple.create(0.0, 1.0)),
                new DirectReceiver.InitContext(SlotType.RATIO, Couple.create(0.0, 1.0)),
                new DirectReceiver.InitContext(SlotType.RATIO, Couple.create(0.0, 1.0))
        );

    }

    @Override
    public int getFlickerScore() {
        return 0;
    }

    public double ratio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        setRatioOnly(ratio);
        refreshKinetics();
    }

    private void refreshKinetics(){
        detachKinetics();
        attachKinetics();
    }

    private void setRatioOnly(double ratio){
        this.ratio = MathUtils.clamp(ratio, 1);
        setChanged();
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource() && face == getBlockState().getValue(FACING).getOpposite()) {
            return clampedModifier();
        }
        return 1;
    }

    public float clampedModifier(){
        float newSpeed = getTheoreticalSpeed() * (float) ratio;
        return ((int) newSpeed) / getTheoreticalSpeed();
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        handler.onRead(compound, clientPacket);
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        handler.onWrite(compound, clientPacket);
    }

    @Override
    public String name() {
        return "resistor";
    }

    @Override
    public NetworkHandler handler() {
        return handler;
    }
}
