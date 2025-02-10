package com.verr1.vscontrolcraft.blocks.slider;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.verr1.vscontrolcraft.base.Constrain.ConstrainCenter;
import com.verr1.vscontrolcraft.base.Constrain.DataStructure.ConstrainKey;
import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.base.DataStructure.SynchronizedField;
import com.verr1.vscontrolcraft.base.Hinge.interfaces.IConstrainHolder;
import com.verr1.vscontrolcraft.base.Servo.ICanBruteDirectionalConnect;
import com.verr1.vscontrolcraft.base.Servo.IPIDController;
import com.verr1.vscontrolcraft.base.Servo.PIDControllerInfoHolder;
import com.verr1.vscontrolcraft.base.ShipConnectorBlockEntity;
import com.verr1.vscontrolcraft.base.UltraTerminal.*;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.constraints.VSFixedOrientationConstraint;
import org.valkyrienskies.core.apigame.constraints.VSSlideConstraint;
import org.valkyrienskies.core.impl.game.ships.ShipDataCommon;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.core.util.datastructures.DenseBlockPosSet;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.assembly.ShipAssemblyKt;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static net.minecraft.ChatFormatting.GRAY;

public class SliderControllerBlockEntity extends ShipConnectorBlockEntity implements
        IPIDController, ICanBruteDirectionalConnect,
        IConstrainHolder, IHaveGoggleInformation,
        ITerminalDevice, IPacketHandler
{

    private final double MAX_SLIDE_DISTANCE = 32;

    public SynchronizedField<ShipPhysics> ownPhysics = new SynchronizedField<>(ShipPhysics.EMPTY);
    public SynchronizedField<ShipPhysics> cmpPhysics = new SynchronizedField<>(ShipPhysics.EMPTY);
    public SynchronizedField<Double> controlForce = new SynchronizedField<>(0.0);

    private final SynchronizedField<Vector3d> cachedPos_Own = new SynchronizedField<>(new Vector3d());
    private final SynchronizedField<Vector3d> cachedPos_Cmp = new SynchronizedField<>(new Vector3d());


    private final LerpedFloat animatedDistance = LerpedFloat.linear();
    public float animatedTargetDistance = 0;

    private SliderControllerPeripheral peripheral;
    protected LazyOptional<IPeripheral> peripheralCap;

    private List<ExposedFieldWrapper> fields = List.of(
            new ExposedFieldWrapper(
                    controlForce::read,
                    controlForce::write,
                    "Force",
                    WidgetType.SLIDE,
                    ExposedFieldType.FORCE
            ),
            new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getTarget(),
                    t -> this.getControllerInfoHolder().setTarget(t),
                    "target",
                    WidgetType.SLIDE,
                    ExposedFieldType.TARGET
            ),
            new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getPIDParams().p(),
                    p -> this.getControllerInfoHolder().setP(p),
                    "P",
                    WidgetType.SLIDE,
                    ExposedFieldType.P
            ),
            new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getPIDParams().i(),
                    i -> this.getControllerInfoHolder().setI(i),
                    "I",
                    WidgetType.SLIDE,
                    ExposedFieldType.I
            ),
            new ExposedFieldWrapper(
                    () -> this.getControllerInfoHolder().getPIDParams().d(),
                    d -> this.getControllerInfoHolder().setD(d),
                    "D",
                    WidgetType.SLIDE,
                    ExposedFieldType.D
            )
    );

    private ExposedFieldWrapper exposedField = fields.get(1);

    @Override
    public ExposedFieldWrapper getExposedField() {
        return exposedField;
    }

    @Override
    public void setExposedField(ExposedFieldType type, double min, double max) {
        switch (type){
            case FORCE -> exposedField = fields.get(0);
            case TARGET -> exposedField = fields.get(1);
            case P -> exposedField = fields.get(2);
            case I -> exposedField = fields.get(3);
            case D -> exposedField = fields.get(4);
        }

        exposedField.min_max = new Vector2d(min, max);
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

    private final PIDControllerInfoHolder controllerInfoHolder = new PIDControllerInfoHolder().setParameter(0.5, 0, 14);

    public SliderControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        lazyTickRate = 20;
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

        VSSlideConstraint slideConstraint = new VSSlideConstraint(
                getServerShipID(),
                assemShipID,
                1.0E-10,
                asmPos_Own,
                asmPos_Asm,
                1.0E10,
                getDirectionJOML(),
                MAX_SLIDE_DISTANCE
        );

        recreateConstrains(hingeConstraint, slideConstraint);
        setCompanionShipID(assemShipID);
        setCompanionShipDirection(align);
        notifyUpdate();

    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        getControllerInfoHolder().setP(speed);
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

        VSSlideConstraint slideConstraint = new VSSlideConstraint(
                getServerShipID(),
                assembledShipID,
                1.0E-10,
                asmPos_Own,
                asmPos_Asm,
                1.0E10,
                getDirectionJOML(),
                MAX_SLIDE_DISTANCE
        );


        recreateConstrains(fixedOrientationConstraint, slideConstraint);
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
            VSSlideConstraint slide
    ) {
        var shipWorldCore = (ShipObjectServerWorld)VSGameUtilsKt.getShipObjectWorld((ServerLevel) level);

        boolean isGrounded = !isOnServerShip();


         ConstrainCenter.createOrReplaceNewConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "hinge", isGrounded, false, false),
                hinge_0,
                shipWorldCore
        );

        ConstrainCenter.createOrReplaceNewConstrain(
                new ConstrainKey(getBlockPos(), getDimensionID(), "slide", isGrounded, false, false),
                slide,
                shipWorldCore
        );

        cachedPos_Own.write(new Vector3d(slide.getLocalPos0()));
        cachedPos_Cmp.write(new Vector3d(slide.getLocalPos1()));
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if(level.isClientSide)return;
        syncCachedPos();
    }


    public void syncCachedPos(){
        VSSlideConstraint slide = (VSSlideConstraint)ConstrainCenter.get(new ConstrainKey(getBlockPos(), getDimensionID(), "slide", !isOnServerShip(), false, false));
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
        ConstrainCenter.remove(new ConstrainKey(getBlockPos(), getDimensionID(), "slide", isGrounded, false, false));
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
            var p = new BlockBoundClientPacket.builder(getBlockPos(), BlockBoundPacketType.SYNC_ANIMATION)
                    .withDouble(getSlideDistance())
                    .build();
            AllPackets.getChannel().send(PacketDistributor.ALL.noArg(), p);
        }
    }

    public void setAnimatedDistance(float d){
        animatedTargetDistance = (float) VSMathUtils.clamp0(d, MAX_SLIDE_DISTANCE);

    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {


        Lang.text("Servo Statistic")
                .style(GRAY)
                .forGoggles(tooltip);

        float distance = getAnimatedTargetDistance(1);

        Lang.number(distance)
                .text("m")
                .style(ChatFormatting.AQUA)
                .space()
                .add(Lang.text("current distance")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
        return true;
    }

    public LogicalSlider getLogicalSlider() {
        if(level.isClientSide)return null;
        ServerShip ship = getCompanionServerShip();
        if(ship == null)return null;
        Vector3dc own_loc = getOwn_loc();
        Vector3dc cmp_loc = getCmp_loc();
        if(own_loc == null || cmp_loc == null)return null;

        return new LogicalSlider(
                getServerShipID(),
                getCompanionShipID(),
                (ServerLevel) level,
                getDirection(),
                own_loc,
                cmp_loc,
                getOutputForce()
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleClient(NetworkEvent.Context context, BlockBoundClientPacket packet) {
        if(packet.getType() == BlockBoundPacketType.SYNC_ANIMATION){
            setAnimatedDistance(packet.getDoubles().get(0).floatValue());
        }
    }

    @Override
    public void handleServer(NetworkEvent.Context context, BlockBoundServerPacket packet) {

    }
}
