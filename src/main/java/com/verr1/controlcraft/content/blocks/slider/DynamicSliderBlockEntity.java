package com.verr1.controlcraft.content.blocks.slider;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.verr1.controlcraft.Config;
import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.content.blocks.SharedKeys;
import com.verr1.controlcraft.content.create.DSliderKineticPeripheral;
import com.verr1.controlcraft.content.valkyrienskies.attachments.DynamicSliderForceInducer;
import com.verr1.controlcraft.foundation.api.delegate.IControllerProvider;
import com.verr1.controlcraft.foundation.api.delegate.IKineticDevice;
import com.verr1.controlcraft.foundation.api.delegate.ITerminalDevice;
import com.verr1.controlcraft.foundation.network.executors.ClientBuffer;
import com.verr1.controlcraft.foundation.network.executors.CompoundTagPort;
import com.verr1.controlcraft.foundation.network.executors.SerializePort;
import com.verr1.controlcraft.content.cctweaked.peripheral.SliderPeripheral;
import com.verr1.controlcraft.foundation.api.*;
import com.verr1.controlcraft.foundation.data.SynchronizedField;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.data.control.DynamicController;
import com.verr1.controlcraft.foundation.data.control.PID;
import com.verr1.controlcraft.foundation.data.field.ExposedFieldWrapper;
import com.verr1.controlcraft.foundation.data.logical.LogicalSlider;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundClientPacket;
import com.verr1.controlcraft.foundation.network.packets.BlockBoundServerPacket;
import com.verr1.controlcraft.foundation.type.*;
import com.verr1.controlcraft.foundation.type.descriptive.CheatMode;
import com.verr1.controlcraft.foundation.type.descriptive.ExposedFieldType;
import com.verr1.controlcraft.foundation.type.descriptive.LockMode;
import com.verr1.controlcraft.foundation.type.descriptive.TargetMode;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.registry.ControlCraftPackets;
import com.verr1.controlcraft.utils.SerializeUtils;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;

import java.lang.Math;
import java.util.List;
import java.util.Optional;

import static com.verr1.controlcraft.content.blocks.SharedKeys.*;

// @SuppressWarnings("unused")
public class DynamicSliderBlockEntity extends AbstractSlider implements
        IControllerProvider, IHaveGoggleInformation,
        ITerminalDevice, IPacketHandler, IKineticDevice
{


    public SynchronizedField<Double> controlForce = new SynchronizedField<>(0.0);


    private boolean isLocked = false;

    private final DynamicController controller = new DynamicController().withPID(DEFAULT_POSITION_MODE_PARAMS);




    private TargetMode targetMode = TargetMode.POSITION;
    private LockMode lockMode = LockMode.OFF;



    private CheatMode cheatMode = CheatMode.NONE;



    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    controlForce::read,
                    controlForce::write,
                    "Force",
                    ExposedFieldType.FORCE
            ).withSuggestedRange(0, 1000),
            new ExposedFieldWrapper(
                    () -> this.getController().getTarget(),
                    t -> this.getController().setTarget(t),
                    "target",
                    ExposedFieldType.TARGET
            ).withSuggestedRange(0, 15),
            new ExposedFieldWrapper(
                    () -> this.getController().getTarget(),
                    t -> this.getController().setTarget(t),
                    "target",
                    ExposedFieldType.TARGET$1
            ).withSuggestedRange(0, 15),
            new ExposedFieldWrapper(
                    () -> (isLocked ? 1.0 : 0.0),
                    (d) -> {
                        if(d > (double) 1 / 15)tryLock();
                        else if(d < (double) 1 / 15)tryUnlock();
                    },
                    "Locked",
                    ExposedFieldType.IS_LOCKED
            ),
            new ExposedFieldWrapper(
                    () -> (isLocked ? 1.0 : 0.0),
                    (d) -> {
                        if(d > (double) 1 / 15)tryLock();
                        else if(d < (double) 1 / 15)tryUnlock();
                    },
                    "Locked",
                    ExposedFieldType.IS_LOCKED$1
            )
    );

    private SliderPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;
    private final DSliderKineticPeripheral kineticPeripheral = new DSliderKineticPeripheral(this);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new SliderPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public DynamicController getController() {
        return controller;
    }

    public double getTarget(){
        return controller.getTarget();
    }

    public void setTarget(double target){
        controller.setTarget(target);
    }


    public LockMode getLockMode() {
        return lockMode;
    }

    public void setLockMode(LockMode lockMode) {
        this.lockMode = lockMode;
    }

    public CheatMode getCheatMode() {
        return cheatMode;
    }

    public void setCheatMode(CheatMode cheatMode) {
        this.cheatMode = cheatMode;
    }

    public void setTargetMode(TargetMode targetMode) {
        if(this.targetMode == targetMode)return;
        this.targetMode = targetMode;
        // delay this because client screen will also call to set PID values of last mode
        Runnable task = () -> {if(targetMode == TargetMode.POSITION){
            controller.PID(DEFAULT_POSITION_MODE_PARAMS);
        }

        if(targetMode == TargetMode.VELOCITY){
            controller.PID(DEFAULT_VELOCITY_MODE_PARAMS);
        }};

        if(level == null || level.isClientSide)return;
        ControlCraftServer.SERVER_EXECUTOR.executeLater(task, 1);
        setChanged();
    }

    public TargetMode getTargetMode() {
        return targetMode;
    }

    public void setModeBoolean(boolean adjustingPosition) {
        targetMode = adjustingPosition ? TargetMode.POSITION : TargetMode.VELOCITY;
        if(adjustingPosition){
            getController().PID(IControllerProvider.DEFAULT_POSITION_MODE_PARAMS);
        }else {
            getController().PID(IControllerProvider.DEFAULT_VELOCITY_MODE_PARAMS);
        }
    }

    public void toggleMode(){
        setModeBoolean(targetMode != TargetMode.POSITION);
    }

    public void setLockMode(boolean softLockMode) {
        lockMode = softLockMode ? LockMode.ON : LockMode.OFF;
        setChanged();
    }

    public void setCheatMode(boolean cheatMode) {
        this.cheatMode = cheatMode ? CheatMode.NO_REPULSE : CheatMode.NONE;
        setChanged();
    }



    public void lock(){
        if(level == null || level.isClientSide)return;


        Ship compShip = getCompanionServerShip();
        if(compShip == null)return;
        long selfId = getShipOrGroundID();
        long compId = compShip.getId();


        Vector3dc sliDir = ValkyrienSkies.set(new Vector3d(), getSlideDirection().getNormal());


        VSAttachmentConstraint fixed = new VSAttachmentConstraint(
                selfId,
                compId,
                1.0E-20,
                context.self().getPos().fma(getSlideDistance(), sliDir, new Vector3d()),
                context.comp().getPos(), // This is the opposite with the case of assemble()
                1.0E20,
                0.0
        );

        overrideConstraint("fix", fixed);

        isLocked = true;
        setChanged();

    }

    public void unlock(){
        if(level == null || level.isClientSide)return;
        removeConstraint("fix");
        isLocked = false;
        setChanged();
    }

    public void lockCheck(){
        if(lockMode != LockMode.ON)return;
        if(targetMode == TargetMode.POSITION){
            if(Math.abs(getSlideDistance() - getTarget()) < 1e-3){
                tryLock();
            }else{
                tryUnlock();
            }
        }
        if(targetMode == TargetMode.VELOCITY){
            if(Math.abs(getTarget()) < 1e-3){
                tryLock();
            }else{
                tryUnlock();
            }
        }
    }

    public void tryLock(){
        if(isLocked)return;
        lock();
    }

    public void tryUnlock(){
        if(!isLocked)return;
        unlock();
    }




    public double getOutputForce(){
        return controlForce.read();
    }

    public void setOutputForce(double force){
        controlForce.write(force);
    }



    public DynamicSliderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        buildRegistry(CHEAT_MODE)
                .withBasic(SerializePort.of(this::getCheatMode, this::setCheatMode, SerializeUtils.ofEnum(CheatMode.class)))
                .withClient(ClientBuffer.of(CheatMode.class))
                .register();

        buildRegistry(TARGET_MODE)
                .withBasic(SerializePort.of(this::getTargetMode, this::setTargetMode, SerializeUtils.ofEnum(TargetMode.class)))
                .withClient(ClientBuffer.of(TargetMode.class))
                .register();

        buildRegistry(LOCK_MODE)
                .withBasic(SerializePort.of(this::getLockMode, this::setLockMode, SerializeUtils.ofEnum(LockMode.class)))
                .withClient(ClientBuffer.of(LockMode.class))
                .register();

        buildRegistry(IS_LOCKED)
                .withBasic(SerializePort.of(this::isLocked, bl -> isLocked = bl, SerializeUtils.BOOLEAN))
                .withClient(ClientBuffer.BOOLEAN.get())
                .register();

        buildRegistry(FIELD)
                .withBasic(CompoundTagPort.of(
                        ITerminalDevice.super::serialize,
                        ITerminalDevice.super::deserializeUnchecked
                ))
                .withClient(
                        new ClientBuffer<>(SerializeUtils.UNIT, CompoundTag.class)
                )
                .dispatchToSync()
                .register();

        buildRegistry(CONTROLLER)
                .withBasic(CompoundTagPort.of(
                        () -> getController().serialize(),
                        tag -> getController().deserialize(tag)
                ))
                .withClient(
                        new ClientBuffer<>(SerializeUtils.UNIT, CompoundTag.class)
                )
                .register();

        buildRegistry(TARGET)
                .withBasic(SerializePort.of(
                        () -> getController().getTarget(),
                        t -> getController().setTarget(t),
                        SerializeUtils.DOUBLE
                ))
                .withClient(
                        new ClientBuffer<>(SerializeUtils.DOUBLE, Double.class)
                )
                .register();

        buildRegistry(VALUE)
                .withBasic(SerializePort.of(
                        () -> getController().getValue(),
                        $ -> {},
                        SerializeUtils.DOUBLE
                ))
                .withClient(
                        new ClientBuffer<>(SerializeUtils.DOUBLE, Double.class)
                )
                .runtimeOnly()
                .register();

        panel().registerUnit(SharedKeys.LOCK, this::tryLock);
        panel().registerUnit(SharedKeys.UNLOCK, this::tryUnlock);
        panel().registerUnit(SharedKeys.DISASSEMBLE, this::destroyConstraints);
        registerConstraintKey("fix");



        lazyTickRate = 20;
        MAX_SLIDE_DISTANCE = Config.PhysicsMaxSlideDistance;
    }

    @Override
    public void destroyConstraints() {
        super.destroyConstraints();
        removeConstraint("fix");
    }

    @Override
    public @NotNull Direction getSlideDirection() {
        return getDirection();
    }


    public void syncAttachInducer(){
        if(level != null && level.isClientSide)return;
        Optional
            .ofNullable(getCompanionServerShip())
            .map(DynamicSliderForceInducer::getOrCreate)
            .ifPresent(inducer -> inducer.replace(
                    WorldBlockPos.of(level, getBlockPos()),
                    this::getLogicalSlider
            ));


    }


    @Override
    public void tickServer() {
        super.tickServer();
        lockCheck();
        syncAttachInducer();
        syncForNear(true, FIELD);
        kineticPeripheral.tick();
    }



    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return ITerminalDevice.super.TerminalDeviceToolTip(tooltip, isPlayerSneaking);
    }

    public LogicalSlider getLogicalSlider() {
        if(noCompanionShip())return null;
        return new LogicalSlider(
                getShipOrGroundID(),
                getCompanionShipID(),
                WorldBlockPos.of(level, getBlockPos()),
                getSlideDirection(),
                context.self().getPos(),
                context.comp().getPos(),
                targetMode == TargetMode.POSITION,
                cheatMode != CheatMode.NO_REPULSE,
                !isLocked(),
                getOutputForce(),
                getController()
        );
    }

    public boolean isLocked() {
        return isLocked;
    }

    protected void displayScreen(ServerPlayer player){

        double t = getController().getTarget();
        double v = getSlideDistance();

        boolean m = targetMode == TargetMode.POSITION;
        boolean l = isLocked();
        boolean c = cheatMode == CheatMode.NO_REPULSE;


        PID pidParams = getController().PID();

        var p = new BlockBoundClientPacket.builder(getBlockPos(), RegisteredPacketType.OPEN_SCREEN_0)
                .withDouble(t)
                .withDouble(v)
                .withDouble(pidParams.p())
                .withDouble(pidParams.i())
                .withDouble(pidParams.d())
                .withBoolean(m)
                .withBoolean(l)
                .withBoolean(c)
                .build();

        ControlCraftPackets.sendToPlayer(p, player);

    }


    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {

        if(packet.getType() == RegisteredPacketType.TOGGLE_0){
            setCheatMode(cheatMode == CheatMode.NONE);
        }
        if(packet.getType() == RegisteredPacketType.TOGGLE_1){
            setLockMode(lockMode == LockMode.OFF);
        }
    }


    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    public String name() {
        return "slider";
    }

    @Override
    public IKineticPeripheral peripheral() {
        return kineticPeripheral;
    }
}
