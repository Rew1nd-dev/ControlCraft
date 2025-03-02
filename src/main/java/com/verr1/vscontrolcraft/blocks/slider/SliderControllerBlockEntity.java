package com.verr1.vscontrolcraft.blocks.slider;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.vscontrolcraft.Config;
import com.verr1.vscontrolcraft.base.Constrain.ConstrainCenter;
import com.verr1.vscontrolcraft.base.Constrain.DataStructure.ConstrainKey;
import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.base.DataStructure.SynchronizedField;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IConstrainHolder;
import com.verr1.vscontrolcraft.base.Servo.ICanBruteDirectionalConnect;
import com.verr1.vscontrolcraft.base.Servo.IPIDController;
import com.verr1.vscontrolcraft.base.Servo.PID;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerInfoHolder;
import com.verr1.vscontrolcraft.base.ShipConnectorBlockEntity;
import com.verr1.vscontrolcraft.base.UltraTerminal.*;
import com.verr1.vscontrolcraft.base.Wand.render.WandRenderer;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.SliderControllerPeripheral;
import com.verr1.vscontrolcraft.compat.valkyrienskies.slider.LogicalSlider;
import com.verr1.vscontrolcraft.compat.valkyrienskies.slider.SliderForceInducer;
import com.verr1.vscontrolcraft.network.IPacketHandler;
import com.verr1.vscontrolcraft.network.packets.BlockBoundClientPacket;
import com.verr1.vscontrolcraft.network.packets.BlockBoundPacketType;
import com.verr1.vscontrolcraft.network.packets.BlockBoundServerPacket;
import com.verr1.vscontrolcraft.registry.AllPackets;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;
import org.valkyrienskies.core.apigame.constraints.VSFixedOrientationConstraint;
import org.valkyrienskies.core.apigame.constraints.VSSlideConstraint;
import org.valkyrienskies.core.impl.game.ships.ShipDataCommon;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.core.util.datastructures.DenseBlockPosSet;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.assembly.ShipAssemblyKt;

import javax.annotation.Nullable;
import java.lang.Math;
import java.util.List;
import java.util.Objects;

import static net.minecraft.ChatFormatting.GRAY;

public class SliderControllerBlockEntity extends ShipConnectorBlockEntity implements
        IPIDController, ICanBruteDirectionalConnect,
        IConstrainHolder, IHaveGoggleInformation,
        ITerminalDevice, IPacketHandler
{

    private double MAX_SLIDE_DISTANCE = 32;

    public SynchronizedField<ShipPhysics> ownPhysics = new SynchronizedField<>(ShipPhysics.EMPTY);
    public SynchronizedField<ShipPhysics> cmpPhysics = new SynchronizedField<>(ShipPhysics.EMPTY);
    public SynchronizedField<Double> controlForce = new SynchronizedField<>(0.0);

    private final SynchronizedField<Vector3d> cachedPos_Own = new SynchronizedField<>(new Vector3d());
    private final SynchronizedField<Vector3d> cachedPos_Cmp = new SynchronizedField<>(new Vector3d());

    private final PIDControllerInfoHolder controllerInfoHolder = new PIDControllerInfoHolder().setParameter(5, 0, 2);

    private final LerpedFloat animatedDistance = LerpedFloat.linear();
    public float animatedTargetDistance = 0;

    private SliderControllerPeripheral peripheral;
    protected LazyOptional<IPeripheral> peripheralCap;

    private final PID defaultPositionModeParams = new PID(24, 0, 14);
    private final PID defaultSpeedModeParams = new PID(10, 0, 0);


    private boolean isCheatMode = false;
    private boolean isLocked = false;
    private boolean isAdjustingPosition = true;
    private boolean isSoftLockMode = false;


    public void setMode(boolean adjustingPosition) {
        isAdjustingPosition = adjustingPosition;
        if(isAdjustingPosition){
            getControllerInfoHolder().setParameter(defaultPositionModeParams);
        }else {
            getControllerInfoHolder().setParameter(defaultSpeedModeParams);
        }
        setChanged();
    }

    public void setSoftLockMode(boolean softLockMode) {
        isSoftLockMode = softLockMode;
        setChanged();
    }

    public void setCheatMode(boolean cheatMode) {
        isCheatMode = cheatMode;
        setChanged();
    }

    public boolean isCheatMode() {
        return isCheatMode;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public boolean isAdjustingPosition() {
        return isAdjustingPosition;
    }

    public boolean isSoftLockMode() {
        return isSoftLockMode;
    }

    public void lock(){
        if(level.isClientSide)return;

        Vector3dc cmpDir = getCompanionShipDirectionJOML();
        Vector3dc sliDir = getDirectionJOML();

        ServerShip assembledShip = getCompanionServerShip();

        if(assembledShip == null)return;
        long ownerShipID = getServerShipID();
        long assemShipID = assembledShip.getId();

        VSAttachmentConstraint fixed = new VSAttachmentConstraint(
                ownerShipID,
                assemShipID,
                1.0E-10,
                new Vector3d(getOwn_loc()).fma(getSlideDistance(), sliDir),
                new Vector3d(getCmp_loc()), // This is the opposite with the case of assemble()
                1.0E10,
                0.0
        );

        ConstrainCenter.createOrReplaceNewConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "fixed", !isOnServerShip(), false, false),
                fixed,
                VSGameUtilsKt.getShipObjectWorld((ServerLevel) level)
        );
        isLocked = true;
        setChanged();

    }

    public void unlock(){
        if(level.isClientSide)return;

        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "fixed", !isOnServerShip(), false, false));
        isLocked = false;
        setChanged();
    }

    public void tryLock(){
        if(isLocked)return;
        lock();
    }

    public void tryUnlock(){
        if(!isLocked)return;
        unlock();
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        updateTargetFromCreate();
        softLockCheck();
        // getControllerInfoHolder().setP(speed);
    }

    public void softLockCheck(){
        if(isSoftLockMode() && Math.abs(speed) < 1e-3)tryLock();
        else if(isSoftLockMode() && !isAdjustingPosition() && Math.abs(getControllerInfoHolder().getTarget()) < 1e-3)tryLock();
        else tryUnlock();
    }

    public void updateTargetFromCreate(){
        double createInput2Omega = speed / 60 * 2 * Math.PI;
        if(!isAdjustingPosition) {
            getControllerInfoHolder().setTarget(createInput2Omega);
        }else{
            double currentTarget = getControllerInfoHolder().getTarget();
            double newTarget = VSMathUtils.clamp(currentTarget + createInput2Omega * 0.05, Config.PhysicsMaxSlideDistance);
            getControllerInfoHolder().setTarget(newTarget);
        }
    }

    private final List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    controlForce::read,
                    controlForce::write,
                    "Force",
                    WidgetType.SLIDE,
                    ExposedFieldType.FORCE
            ).withSuggestedRange(0, 1000),
            new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getTarget(),
                    t -> this.getControllerInfoHolder().setTarget(t),
                    "target",
                    WidgetType.SLIDE,
                    ExposedFieldType.TARGET
            ).withSuggestedRange(0, 15),
            new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getTarget(),
                    t -> this.getControllerInfoHolder().setTarget(t),
                    "target",
                    WidgetType.SLIDE,
                    ExposedFieldType.TARGET$1
            ).withSuggestedRange(0, 15),
            new ExposedFieldWrapper(
                    () -> (isLocked ? 1.0 : 0.0),
                    (d) -> {
                        if(d > (double) 1 / 15)tryLock();
                        else if(d < (double) 1 / 15)tryUnlock();
                    },
                    "Locked",
                    WidgetType.SLIDE,
                    ExposedFieldType.IS_LOCKED$1
            ),
            new ExposedFieldWrapper(
                    () -> (isLocked ? 1.0 : 0.0),
                    (d) -> {
                        if(d > (double) 1 / 15)tryLock();
                        else if(d < (double) 1 / 15)tryUnlock();
                    },
                    "Locked",
                    WidgetType.SLIDE,
                    ExposedFieldType.IS_LOCKED$2
            )
    );

    /*
    new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getPIDParams().p(),
                    p -> this.getControllerInfoHolder().setP(p),
                    "P",
                    WidgetType.SLIDE,
                    ExposedFieldType.P
            ).withSuggestedRange(2, 15),
            new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getPIDParams().i(),
                    i -> this.getControllerInfoHolder().setI(i),
                    "I",
                    WidgetType.SLIDE,
                    ExposedFieldType.I
            ).withSuggestedRange(0, 3),
            new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getPIDParams().d(),
                    d -> this.getControllerInfoHolder().setD(d),
                    "D",
                    WidgetType.SLIDE,
                    ExposedFieldType.D
            ).withSuggestedRange(2, 10),
    * */

    private ExposedFieldWrapper exposedField = fields.get(0);

    @Override
    public ExposedFieldWrapper getExposedField() {
        return exposedField;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new SliderControllerPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }



    public SliderControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        lazyTickRate = 20;
        MAX_SLIDE_DISTANCE = Config.PhysicsMaxSlideDistance;
    }

    public Direction getVertical(){
        if(getDirection().getAxis() != Direction.Axis.Y)return Direction.UP;
        return Direction.SOUTH;
    }

    public Quaterniondc getQuaternionOfPlacement(){
        return VSMathUtils.getQuaternionOfPlacement(getDirection());
    }

    public BlockPos getAssembleBlockPos(){
        return getBlockPos().relative(getDirection());
    }

    public Vector3d getAssembleBlockPosJOML(){
        return Util.Vec3toVector3d(getAssembleBlockPos().getCenter());
    }

    @Override
    public void bruteDirectionalConnectWith(BlockPos assemPos, Direction forward, Direction align){
        if(!VSMathUtils.isVertical(align, forward))return;


        Direction selfAlign = getAlign();
        Direction selfForward = getForward();

        ServerShip assembledShip = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) level, assemPos);

        if(assembledShip == null)return;
        long ownerShipID = getServerShipID();
        long assemShipID = assembledShip.getId();

        Quaterniondc hingeQuaternion_Own = VSMathUtils.getQuaternionOfPlacement(selfAlign, selfForward).conjugate();
        Quaterniondc hingeQuaternion_Asm = VSMathUtils.getQuaternionOfPlacement(align.getOpposite(), forward).conjugate();


        VSFixedOrientationConstraint hingeConstraint = new VSFixedOrientationConstraint(
                ownerShipID,
                assemShipID,
                1.0E-10,
                hingeQuaternion_Asm, //new Quaterniond(),//
                hingeQuaternion_Own, //new Quaterniond(),//
                1.0E10
        );

        Vector3dc asmPos_Own = getAssembleBlockPosJOML();
        Vector3dc asmPos_Asm = Util.Vec3toVector3d(assemPos.getCenter());

        VSSlideConstraint slideConstraint_1 = new VSSlideConstraint(
                getServerShipID(),
                assemShipID,
                1.0E-10,
                asmPos_Own,
                asmPos_Asm,
                1.0E10,
                getDirectionJOML(),
                MAX_SLIDE_DISTANCE
        );


        Vector3dc dir_cmp = Util.Vec3itoVector3d(align.getOpposite().getNormal());

        VSSlideConstraint slideConstraint_2 = new VSSlideConstraint(
                getServerShipID(),
                assemShipID,
                1.0E-10,
                asmPos_Own.fma(1, getDirectionJOML(), new Vector3d()),
                asmPos_Asm.fma(1, dir_cmp, new Vector3d()),
                1.0E10,
                getDirectionJOML(),
                MAX_SLIDE_DISTANCE
        );

        recreateConstrains(hingeConstraint, slideConstraint_1, slideConstraint_2);
        setCompanionShipID(assemShipID);
        setCompanionShipDirection(align);
        notifyUpdate();

    }



    @Override
    public Direction getAlign() {
        return getDirection();
    }

    @Override
    public Direction getForward() {
        return getVertical().getOpposite();
    }

    public double getOutputForce(){
        return controlForce.read();
    }

    public void setOutputForce(double force){
        controlForce.write(force);
    }

    public void assemble(){
        // Only Assemble 1 Block When Being Right-Clicked with wrench. You Should Build Your Ship Up On This Assembled Block, Or Else Use Linker Tool Instead
        if(level.isClientSide)return;
        ServerLevel serverLevel = (ServerLevel) level;
        DenseBlockPosSet collectedBlocks = new DenseBlockPosSet();
        BlockPos assembledShipCenter = getAssembleBlockPos();
        if(serverLevel.getBlockState(assembledShipCenter).isAir())return;
        collectedBlocks.add(assembledShipCenter.getX(), assembledShipCenter.getY(), assembledShipCenter.getZ());
        ServerShip assembledShip = ShipAssemblyKt.createNewShipWithBlocks(assembledShipCenter, collectedBlocks, serverLevel);
        long assembledShipID = assembledShip.getId();

        Quaterniondc ownerShipQuaternion = getSelfShipQuaternion();


        Vector3d assembledShipPosCenterShipAtOwner = getAssembleBlockPosJOML();
        Vector3d assembledShipPosCenterWorld = new Vector3d(assembledShipPosCenterShipAtOwner);
        if(isOnServerShip()){
            assembledShipPosCenterWorld = Objects.requireNonNull(getServerShipOn()).getShipToWorld().transformPosition(assembledShipPosCenterWorld);
        }

        long ownerShipID = getServerShipID();

        ((ShipDataCommon)assembledShip)
                .setTransform(
                        new ShipTransformImpl(
                                assembledShipPosCenterWorld,
                                assembledShip
                                        .getInertiaData()
                                        .getCenterOfMassInShip(),
                                ownerShipQuaternion,
                                new Vector3d(1, 1, 1)
                        )
                );



        VSFixedOrientationConstraint fixedOrientationConstraint = new VSFixedOrientationConstraint(
                ownerShipID,
                assembledShipID,
                1.0E-10,
                new Quaterniond(),
                new Quaterniond(),
                1.0E10
        );

        Vector3dc asmPos_Own = getAssembleBlockPosJOML();
        Vector3dc asmPos_Asm = assembledShip.getInertiaData().getCenterOfMassInShip().add(new Vector3d(0.5, 0.5, 0.5), new Vector3d());




        VSSlideConstraint slideConstraint_1 = new VSSlideConstraint(
                getServerShipID(),
                assembledShipID,
                1.0E-20,
                asmPos_Own,
                asmPos_Asm,
                1.0E20,
                getDirectionJOML(),
                MAX_SLIDE_DISTANCE
        );

        VSSlideConstraint slideConstraint_2 = new VSSlideConstraint(
                getServerShipID(),
                assembledShipID,
                1.0E-20,
                asmPos_Own.fma(1, getDirectionJOML(), new Vector3d()),
                asmPos_Asm.fma(1, getDirectionJOML(), new Vector3d()),
                1.0E20,
                getDirectionJOML(),
                MAX_SLIDE_DISTANCE
        );


        recreateConstrains(fixedOrientationConstraint, slideConstraint_1, slideConstraint_2);
        setCompanionShipID(assembledShipID);
        setCompanionShipDirection(Direction.DOWN);
        notifyUpdate();

    }


    public @NotNull Vector3dc getOwn_loc(){
        return cachedPos_Own.read();
    }

    public @NotNull Vector3dc getCmp_loc(){
        return cachedPos_Cmp.read();
    }

    @Override
    public PIDControllerInfoHolder getControllerInfoHolder(){
        return controllerInfoHolder;
    }

    public double getSlideDistance(){
        if(!hasCompanionShip())return 0;
        if(cmpPhysics.read().mass() < 1e-2)return 0;
        Vector3dc own_local_pos = getOwn_loc();
        Vector3dc cmp_local_pos = getCmp_loc();

        ShipPhysics own_sp = ownPhysics.read();
        ShipPhysics cmp_sp = cmpPhysics.read();

        Matrix4dc own_s2w = own_sp.s2wTransform();
        Matrix4dc own_w2s = own_sp.w2sTransform();
        Matrix4dc cmp_s2w = cmp_sp.s2wTransform();

        Vector3dc own_wc = own_s2w.transformPosition(own_local_pos, new Vector3d());
        Vector3dc cmp_wc = cmp_s2w.transformPosition(cmp_local_pos, new Vector3d());
        Vector3dc sub_sc = own_w2s
                .transformDirection(
                        cmp_wc.sub(own_wc, new Vector3d()), new Vector3d()
                );

        Direction dir = getDirection();
        double sign = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;
        double distance = switch (dir.getAxis()){
            case X -> sign * sub_sc.x();
            case Y -> sign * sub_sc.y();
            case Z -> sign * sub_sc.z();
        };

        return distance;
    }

    public void recreateConstrains(
            VSFixedOrientationConstraint hinge_0,
            VSSlideConstraint slide_1,
            VSSlideConstraint slide_2
    ) {
        var shipWorldCore = (ShipObjectServerWorld)VSGameUtilsKt.getShipObjectWorld((ServerLevel) level);

        boolean isGrounded = !isOnServerShip();


        ConstrainCenter.createOrReplaceNewConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "hinge", isGrounded, false, false),
                hinge_0,
                shipWorldCore
        );


        ConstrainCenter.createOrReplaceNewConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "slide_1", isGrounded, false, false),
                slide_1,
                shipWorldCore
        );

        ConstrainCenter.createOrReplaceNewConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "slide_2", isGrounded, false, false),
                slide_2,
                shipWorldCore
        );



        cachedPos_Own.write(new Vector3d(slide_1.getLocalPos0()));
        cachedPos_Cmp.write(new Vector3d(slide_1.getLocalPos1()));
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if(level.isClientSide)return;
        syncCachedPos();
        syncClient(getBlockPos(), level);



    }


    public void syncCachedPos(){
        VSSlideConstraint slide = (VSSlideConstraint)ConstrainCenter.get(new ConstrainKey(getBlockPos(), getDimensionID(), "slide_1", !isOnServerShip(), false, false));
        if(slide == null)return;
        cachedPos_Own.write(new Vector3d(slide.getLocalPos0()));
        cachedPos_Cmp.write(new Vector3d(slide.getLocalPos1()));
    }

    @Override
    public void destroy() {
        super.destroy();
        if(level.isClientSide)return;
        destroyConstrain();
    }

    public void destroyConstrain(){
        boolean isGrounded = !isOnServerShip();
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "hinge", isGrounded, false, false));
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "slide_1", isGrounded, false, false));
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "slide_2", isGrounded, false, false));
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "fixed", !isOnServerShip(), false, false));
        clearCompanionShipInfo();
    }

    public void syncCompanionAttachInducer(){
        if(level.isClientSide)return;
        ServerShip ship = getCompanionServerShip();
        if(ship == null)return;
        var inducer = SliderForceInducer.getOrCreate(ship);
        inducer.update(new LevelPos(getBlockPos(), (ServerLevel) level));
    }

    @Override
    public void tick() {
        super.tick();
        syncClient();
        syncCompanionAttachInducer();

        if(isAdjustingPosition)updateTargetFromCreate();
        // softLockCheck();

        if(!level.isClientSide)return;
        tickAnimation();

    }



    public void tickAnimation(){
        animatedDistance.chase(animatedTargetDistance, 0.5, LerpedFloat.Chaser.EXP);
        animatedDistance.tickChaser();
    }

    float getAnimatedTargetDistance(float partialTicks){
        return animatedDistance.getValue(partialTicks);
    }

    public void syncClient(){
        if(!level.isClientSide){
            var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.SYNC_0)
                    .withDouble(getSlideDistance())
                    .build();
            AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    public void setAnimatedDistance(float d){
        animatedTargetDistance = (float) VSMathUtils.clamp0(d, MAX_SLIDE_DISTANCE);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {


        Lang.text("Piston Statistic")
                .style(GRAY)
                .forGoggles(tooltip);

        float distance = getAnimatedTargetDistance(1);

        Lang.number(distance)
                .text("m")
                .style(ChatFormatting.AQUA)
                .space()
                .forGoggles(tooltip, 1);


        Direction dir = WandRenderer.lookingAtFaceDirection();
        if(dir == null)return true;
        tooltip.add(Components.literal("Face " + dir + " Bounded:"));
        fields().forEach(f -> {
            if(!f.directionOptional.test(dir))return;
            String info = f.type.getComponent().getString();
            tooltip.add(Component.literal(info).withStyle(ChatFormatting.AQUA));
        });

        return true;

    }


    public LogicalSlider getLogicalSlider() {
        if(level.isClientSide)return null;
        ServerShip ship = getCompanionServerShip();
        if(ship == null)return null;
        Vector3dc own_loc = getOwn_loc();
        Vector3dc cmp_loc = getCmp_loc();

        return new LogicalSlider(
                getServerShipID(),
                getCompanionShipID(),
                (ServerLevel) level,
                getDirection(),
                own_loc,
                cmp_loc,
                getOutputForce(),
                isAdjustingPosition(),
                !isCheatMode()
        );
    }

    @Override
    public List<ExposedFieldWrapper> fields() {
        return fields;
    }

    @Override
    public String name() {
        return "Slider Device";
    }

    protected void displayScreen(ServerPlayer player){

        double t = getControllerInfoHolder().getTarget();
        double v = getControllerInfoHolder().getValue();

        boolean m = isAdjustingPosition();
        boolean l = isLocked();
        boolean c = isCheatMode();


        PID pidParams = getControllerInfoHolder().getPIDParams();

        var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.OPEN_SCREEN_0)
                .withDouble(t)
                .withDouble(v)
                .withDouble(pidParams.p())
                .withDouble(pidParams.i())
                .withDouble(pidParams.d())
                .withBoolean(m)
                .withBoolean(l)
                .withBoolean(c)
                .build();

        AllPackets.sendToPlayer(p, player);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == BlockBoundPacketType.OPEN_SCREEN_0){
            double t = packet.getDoubles().get(0);
            double v = packet.getDoubles().get(1);
            double p = packet.getDoubles().get(2);
            double i = packet.getDoubles().get(3);
            double d = packet.getDoubles().get(4);
            boolean m = packet.getBooleans().get(0);
            boolean l = packet.getBooleans().get(1);
            boolean c = packet.getBooleans().get(2);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    ScreenOpener.open(new SliderScreen(getBlockPos(), p, i, d, v, t, m, l, c)));
        }
        if(packet.getType() == BlockBoundPacketType.SYNC_0){
            setAnimatedDistance(packet.getDoubles().get(0).floatValue());
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {

        if(packet.getType() == BlockBoundPacketType.TOGGLE_0){
            setCheatMode(!isCheatMode());
        }
        if(packet.getType() == BlockBoundPacketType.TOGGLE_1){
            setSoftLockMode(!isSoftLockMode());
        }
    }


    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if(clientPacket)return;
        fields.forEach(e -> tag.put("field_" + e.type.name(), e.serialize()));
        tag.putInt("exposedField", fields.indexOf(exposedField));
        tag.put("controller", getControllerInfoHolder().serialize());
        tag.putBoolean("cheatMode", isCheatMode());
        tag.putBoolean("isLocked", isLocked);
        tag.putBoolean("positionMode", isAdjustingPosition);
        tag.putBoolean("softLockMode", isSoftLockMode);

    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if(clientPacket)return;

        fields.forEach(f -> f.deserialize(tag.getCompound("field_" + f.type.name())));
        try{
            exposedField = fields.get(tag.getInt("exposed_field"));
        }catch (IndexOutOfBoundsException e){
            exposedField = fields.get(0);
        }
        getControllerInfoHolder().deserialize(tag.getCompound("controller"));
        isCheatMode = tag.getBoolean("cheatMode");
        isLocked = tag.getBoolean("isLocked");
        isAdjustingPosition = tag.getBoolean("positionMode");
        isSoftLockMode = tag.getBoolean("softLockMode");

        if(!isAdjustingPosition){
            getControllerInfoHolder().setTarget(0);
        }

    }




}
