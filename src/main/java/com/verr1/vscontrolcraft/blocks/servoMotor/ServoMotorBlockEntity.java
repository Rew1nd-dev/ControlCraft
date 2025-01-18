package com.verr1.vscontrolcraft.blocks.servoMotor;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.ServoMotorPeripheral;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.SpinalyzerPeripheral;
import com.verr1.vscontrolcraft.compat.valkyrienskies.generic.QueueForceInducer;
import com.verr1.vscontrolcraft.compat.valkyrienskies.servo.LogicalServoMotor;
import com.verr1.vscontrolcraft.compat.valkyrienskies.servo.ServoMotorForceInducer;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;
import org.valkyrienskies.core.apigame.constraints.VSHingeOrientationConstraint;
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
import java.util.Optional;

import static net.minecraft.ChatFormatting.GRAY;

public class ServoMotorBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    private long assembledShipID;

    private boolean assembleNextTick = false;
    private final controllerInfoHolder servoController = new controllerInfoHolder();

    private Direction assembleShipDirection;

    private final Object asm_lock = new Object();
    private final Object own_lock = new Object();
    private ShipPhysics physicsThreadShipInfo_Own = ShipPhysics.EMPTY;
    private ShipPhysics physicsThreadShipInfo_Asm = ShipPhysics.EMPTY;


    private VSHingeOrientationConstraint savedHinge = null;
    private VSAttachmentConstraint savedAttach_1 = null;
    private VSAttachmentConstraint savedAttach_2 = null;

    public double debug_angle_accessor;

    private ServoMotorPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    public controllerInfoHolder getControllerInfoHolder(){
        return servoController;
    }

    public ServoMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new ServoMotorPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public @Nullable ServerShip getServerShipOn(){
        if(level.isClientSide)return null;
        ServerShip ship = VSGameUtilsKt.getShipManagingPos((ServerLevel) level, getBlockPos());
        return ship;
    }

    public long getServerShipID(){
        ServerShip ship = getServerShipOn();
        if(ship != null)return ship.getId();
        String dimensionID = getDimensionID();
        ShipObjectServerWorld sosw = (ShipObjectServerWorld)VSGameUtilsKt.getShipObjectWorld(level);
        return sosw.getDimensionToGroundBodyIdImmutable().get(dimensionID);
    }

    public String getDimensionID(){
        return VSGameUtilsKt.getDimensionId(level);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("tooltip.stressImpact")
                .style(GRAY)
                .forGoggles(tooltip);

        float stressTotal = (float)debug_angle_accessor;

        Lang.number(stressTotal)
                .translate("generic.unit.stress")
                .style(ChatFormatting.AQUA)
                .space()
                .add(Lang.translate("gui.goggles.at_current_speed")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
        return true;
    }

    public boolean isOnServerShip(){
        return getServerShipOn() != null;
    }

    public Quaterniondc getOwnerShipQuaternion(){
        Quaterniond q = new Quaterniond();
        Optional.ofNullable(getServerShipOn()).ifPresent((serverShip -> serverShip.getTransform().getShipToWorldRotation().get(q)));
        return q;
    }

    public Quaterniondc getQuaternionOfPlacement(){
        return VSMathUtils.getQuaternionOfPlacement(getBlockState().getValue(ServoMotorBlock.FACING));
    }

    public void writeOwnPhysicsShipInfo(ShipPhysics sp){
        synchronized (own_lock){
            physicsThreadShipInfo_Own = sp;
        }
    }

    public void writeAsmPhysicsShipInfo(ShipPhysics sp){
        synchronized (asm_lock){
            physicsThreadShipInfo_Asm = sp;
        }
    }

    public ShipPhysics readOwnPhysicsShipInfo(){
        synchronized (own_lock){
            return physicsThreadShipInfo_Own;
        }
    }

    public ShipPhysics readAsmPhysicsShipInfo(){
        synchronized (asm_lock){
            return physicsThreadShipInfo_Asm;
        }
    }

    public double getServoAngle(){
        Matrix3dc own = physicsThreadShipInfo_Own.rotationMatrix();
        Matrix3dc asm = physicsThreadShipInfo_Asm.rotationMatrix();
        return VSMathUtils.get_xc2yc(own, asm, getServoDirection(), getAssembleShipDirection());
    }

    public void applyTorque(double torque){
        ServerShip asm = getAssembledServerShip();
        ServerShip own = getServerShipOn();
        if(asm == null)return;
        QueueForceInducer qfi = QueueForceInducer.getOrCreate(asm);
        Vector3dc torque_sc = Util.Vec3itoVector3d(getServoDirection().getNormal()).mul(torque);
        Vector3dc torque_wc = VSMathUtils.get_sc2wc(own).transform(torque_sc, new Vector3d());
        qfi.applyInvariantTorque(torque_wc);
    }

    public void assemble(BlockPos assemPos, Direction assemDir){
        Direction servoDir = getServoDirection();
        Vector3dc ownDir = getServoDirectionJOML();
        Vector3dc asmDir = Util.Vec3itoVector3d(assemDir.getNormal());

        ServerShip assembledShip = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) level, assemPos);
        if(assembledShip == null)return;
        long ownerShipID = getServerShipID();
        long assemShipID = assembledShip.getId();
        Quaterniondc hingeQuaternion_Own = new
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(servoDir))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();

        Quaterniondc hingeQuaternion_Asm = new
                Quaterniond(VSMathUtils.getQuaternionOfPlacement(assemDir.getOpposite()))
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();


        VSHingeOrientationConstraint hingeConstraint = new VSHingeOrientationConstraint(
                assemShipID,
                ownerShipID,
                1.0E-10,
                hingeQuaternion_Asm, //new Quaterniond(),//
                hingeQuaternion_Own, //new Quaterniond(),//
                1.0E10
        );

        Vector3dc asmPos_Own = Util.Vec3toVector3d(getBlockPos().relative(getBlockState().getValue(ServoMotorBlock.FACING)).getCenter());
        Vector3dc asmPos_Asm = Util.Vec3toVector3d(assemPos.getCenter());

        VSAttachmentConstraint attachment_1 = new VSAttachmentConstraint(
                ownerShipID,
                assemShipID,
                1.0E-10,
                new Vector3d(asmPos_Own).fma(1.1, ownDir),
                new Vector3d(asmPos_Asm).fma(-1, asmDir), // This is the opposite with the case of assemble()
                1.0E10,
                0.0
        );

        VSAttachmentConstraint attachment_2 = new VSAttachmentConstraint(
                ownerShipID,
                assemShipID,
                1.0E-10,
                new Vector3d(asmPos_Own).fma(-1, ownDir),
                new Vector3d(asmPos_Asm).fma(1.1, asmDir), // This is the opposite with the case of assemble()
                1.0E10,
                0.0
        );

        saveConstrains(hingeConstraint, attachment_1, attachment_2);
        recreateConstrains();
        setAssembledShipID(assemShipID);
        setAssembleShipDirection(assemDir);
        notifyUpdate();

    }

    public void assemble(){
        // Only Assemble 1 Block When Being Right-Clicked. You Should Build Your Ship Up On This Assembled Block, Or Else Use Linker Tool Instead
        if(level.isClientSide)return;
        ServerLevel serverLevel = (ServerLevel) level;
        DenseBlockPosSet collectedBlocks = new DenseBlockPosSet();
        BlockPos assembledShipCenter = getBlockPos().relative(getBlockState().getValue(ServoMotorBlock.FACING));
        if(serverLevel.getBlockState(assembledShipCenter).isAir())return;
        collectedBlocks.add(assembledShipCenter.getX(), assembledShipCenter.getY(), assembledShipCenter.getZ());
        ServerShip assembledShip = ShipAssemblyKt.createNewShipWithBlocks(assembledShipCenter, collectedBlocks, serverLevel);
        long assembledShipID = assembledShip.getId();

        Quaterniondc ownerShipQuaternion = getOwnerShipQuaternion();
        Vector3d direction = Util.Vec3itoVector3d(getBlockState().getValue(ServoMotorBlock.FACING).getNormal());


        Vector3d assembledShipPosCenterShipAtOwner = Util.Vec3toVector3d(assembledShipCenter.getCenter());
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



        Quaterniondc hingeQuaternion = new
                Quaterniond(getQuaternionOfPlacement())
                .mul(new Quaterniond(new AxisAngle4d(Math.toRadians(90.0), 0.0, 0.0, 1.0)), new Quaterniond())
                .normalize();


        VSHingeOrientationConstraint hingeConstraint = new VSHingeOrientationConstraint(
                assembledShipID,
                ownerShipID,
                1.0E-10,
                hingeQuaternion,
                hingeQuaternion,
                1.0E10
        );

        Vector3dc asmPos_Own = Util.Vec3toVector3d(assembledShipCenter.getCenter());
        Vector3dc asmPos_Asm = assembledShip.getInertiaData().getCenterOfMassInShip().add(new Vector3d(0.5, 0.5, 0.5), new Vector3d());

        VSAttachmentConstraint attachment_1 = new VSAttachmentConstraint(
                ownerShipID,
                assembledShipID,
                1.0E-10,
                new Vector3d(asmPos_Own).fma(1.1, direction),
                new Vector3d(asmPos_Asm).fma(1, direction),
                1.0E10,
                0.0
        );

        VSAttachmentConstraint attachment_2 = new VSAttachmentConstraint(
                ownerShipID,
                assembledShipID,
                1.0E-10,
                new Vector3d(asmPos_Own).fma(-1, direction),
                new Vector3d(asmPos_Asm).fma(-1.1, direction),
                1.0E10,
                0.0
        );



        saveConstrains(hingeConstraint, attachment_1, attachment_2);
        recreateConstrains();
        setAssembledShipID(assembledShipID);
        setAssembleShipDirection(getServoDirection().getOpposite());
        notifyUpdate();

    }

    public void setAssembledShipID(long id){
        this.assembledShipID = id;
    }

    public void setAssembleShipDirection(Direction direction){
        this.assembleShipDirection = direction;
    }

    public Direction getAssembleShipDirection(){
        return this.assembleShipDirection;
    }

    public void saveConstrains(
            VSHingeOrientationConstraint hinge,
            VSAttachmentConstraint attach_1,
            VSAttachmentConstraint attach_2)
    {
        savedHinge = hinge;
        savedAttach_1 = attach_1;
        savedAttach_2 = attach_2;
    }

    public void recreateConstrains(){
        if(savedHinge == null || savedAttach_1 == null || savedAttach_2 == null)return;
        if(level.isClientSide)return;
        var shipWorldCore = (ShipObjectServerWorld)VSGameUtilsKt.getShipObjectWorld(level);
        Object o1 =  shipWorldCore.createNewConstraint(savedHinge);
        Object o2 =  shipWorldCore.createNewConstraint(savedAttach_1);
        Object o3 =  shipWorldCore.createNewConstraint(savedAttach_2);

         if(o1 == null || o2 == null || o3 == null){
            savedHinge = null;
            savedAttach_1 = null;
            savedAttach_2 = null;
        }


    }

    public ServerShip getAssembledServerShip(){
        var shipWorldCore = (ShipObjectServerWorld)VSGameUtilsKt.getShipObjectWorld(level);
        ServerShip assembledShip = shipWorldCore.getLoadedShips().getById(assembledShipID);
        return assembledShip;
    }

    public void writeSavedConstrains(CompoundTag tag){
        tag.putBoolean("assembled", getAssembledServerShip() != null);
        if(savedHinge == null || savedAttach_1 == null || savedAttach_2 == null)return;
        tag.putLong("asm", assembledShipID);
        tag.putLong("own", getServerShipID());
        tag.putString("assemDir", assembleShipDirection.getSerializedName());

        tag.putDouble("q_1_x", savedHinge.component4().x());
        tag.putDouble("q_1_y", savedHinge.component4().y());
        tag.putDouble("q_1_z", savedHinge.component4().z());
        tag.putDouble("q_1_w", savedHinge.component4().w());

        tag.putDouble("q_2_x", savedHinge.component5().x());
        tag.putDouble("q_2_y", savedHinge.component5().y());
        tag.putDouble("q_2_z", savedHinge.component5().z());
        tag.putDouble("q_2_w", savedHinge.component5().w());

        tag.putDouble("a1_p_1x", savedAttach_1.component4().x());
        tag.putDouble("a1_p_1y", savedAttach_1.component4().y());
        tag.putDouble("a1_p_1z", savedAttach_1.component4().z());
        tag.putDouble("a1_p_2x", savedAttach_1.component5().x());
        tag.putDouble("a1_p_2y", savedAttach_1.component5().y());
        tag.putDouble("a1_p_2z", savedAttach_1.component5().z());

        tag.putDouble("a2_p_1x", savedAttach_2.component4().x());
        tag.putDouble("a2_p_1y", savedAttach_2.component4().y());
        tag.putDouble("a2_p_1z", savedAttach_2.component4().z());
        tag.putDouble("a2_p_2x", savedAttach_2.component5().x());
        tag.putDouble("a2_p_2y", savedAttach_2.component5().y());
        tag.putDouble("a2_p_2z", savedAttach_2.component5().z());

    }


    public void readSavedConstrains(CompoundTag tag){
        boolean assembled = tag.getBoolean("assembled");
        if(!assembled)return;
        long asm = tag.getLong("asm");
        long own = tag.getLong("own");
        String assemDirString = tag.getString("assemDir");
        Direction assemDir = Direction.byName(assemDirString);
        double q_1_x = tag.getDouble("q_1_x");
        double q_1_y = tag.getDouble("q_1_y");
        double q_1_z = tag.getDouble("q_1_z");
        double q_1_w = tag.getDouble("q_1_w");

        double q_2_x = tag.getDouble("q_2_x");
        double q_2_y = tag.getDouble("q_2_y");
        double q_2_z = tag.getDouble("q_2_z");
        double q_2_w = tag.getDouble("q_2_w");

        double a1_p_1x = tag.getDouble("a1_p_1x");
        double a1_p_1y = tag.getDouble("a1_p_1y");
        double a1_p_1z = tag.getDouble("a1_p_1z");
        double a1_p_2x = tag.getDouble("a1_p_2x");
        double a1_p_2y = tag.getDouble("a1_p_2y");
        double a1_p_2z = tag.getDouble("a1_p_2z");

        double a2_p_1x = tag.getDouble("a2_p_1x");
        double a2_p_1y = tag.getDouble("a2_p_1y");
        double a2_p_1z = tag.getDouble("a2_p_1z");
        double a2_p_2x = tag.getDouble("a2_p_2x");
        double a2_p_2y = tag.getDouble("a2_p_2y");
        double a2_p_2z = tag.getDouble("a2_p_2z");

        Quaterniond q_1 = new Quaterniond(q_1_x, q_1_y, q_1_z, q_1_w);
        Quaterniond q_2 = new Quaterniond(q_2_x, q_2_y, q_2_z, q_2_w);
        Vector3d a1_p_1 = new Vector3d(a1_p_1x, a1_p_1y, a1_p_1z);
        Vector3d a1_p_2 = new Vector3d(a1_p_2x, a1_p_2y, a1_p_2z);

        Vector3d a2_p_1 = new Vector3d(a2_p_1x, a2_p_1y, a2_p_1z);
        Vector3d a2_p_2 = new Vector3d(a2_p_2x, a2_p_2y, a2_p_2z);

        assembledShipID = asm;

        assembleShipDirection = assemDir;

        savedHinge = new VSHingeOrientationConstraint(
                asm,
                own,
                1.0E-10,
                q_1,
                q_2,
                1.0E10
        );

        savedAttach_1 = new VSAttachmentConstraint(
                own,
                asm,
                1.0E-10,
                a1_p_1,
                a1_p_2,
                1.0E10,
                0.0
        );

        savedAttach_2 = new VSAttachmentConstraint(
                own,
                asm,
                1.0E-10,
                a2_p_1,
                a2_p_2,
                1.0E10,
                0.0
        );



    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        if(clientPacket)return;
        writeSavedConstrains(tag);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if(clientPacket)return;
        try {
            readSavedConstrains(tag);
            DeferralExecutor.executeLater(this::recreateConstrains, 1);
        }catch (Exception e){
            ControlCraft.LOGGER.info("Failed to read saved constrains");
        }

    }

    public Vector3d getServoDirectionJOML(){
        return Util.Vec3itoVector3d(getBlockState().getValue(ServoMotorBlock.FACING).getNormal()) ;
    }

    public Direction getServoDirection(){
        return getBlockState().getValue(ServoMotorBlock.FACING);
    }

    public void setAssembleNextTick(){
        assembleNextTick = true;
    }

    public long getAssembledShipID(){
        return assembledShipID;
    }

    @Override
    public void tick() {
        super.tick();
        if(assembleNextTick){
            assemble();
            assembleNextTick = false;
        }

        syncAssemAttachInducer();
    }

    public void syncAssemAttachInducer(){
        if(level.isClientSide)return;
        ServerShip ship = getAssembledServerShip();
        if(ship == null)return;
        var inducer = ServoMotorForceInducer.getOrCreate(ship);
        inducer.updateLogicalServoMotor(
                getBlockPos(),
                new LogicalServoMotor(
                        getServerShipID(),
                        getAssembledShipID(),
                        (ServerLevel) level,
                        getServoDirection(),
                        getAssembleShipDirection()
                )
        );
    }


    public record pid(double p, double i, double d){}


    public static class controllerInfoHolder {
        private double curr_err = 0;
        private double prev_err = 0;

        private double integral_err = 0;
        private double MAX_INTEGRAL = 10;

        private double p = 24;
        private double d = 4;
        private double i = 0;
        private double ts = 0.01667; // assuming servo controlled by physics thread

        private double targetAngle = 1.57;

        public synchronized controllerInfoHolder overrideError(double angle){
            prev_err = curr_err;
            curr_err = targetAngle - angle;
            integral_err = VSMathUtils.clamp(integral_err + curr_err * ts, MAX_INTEGRAL) ;
            return this;
        }

        public synchronized controllerInfoHolder setTargetAngle(double angle){
            targetAngle = angle;
            return this;
        }



        public double calculateControlTorqueScale(){
            double scale = p * curr_err + d * VSMathUtils.radErrFix(curr_err - prev_err) / ts + i * integral_err;
            return scale;
        }

        public controllerInfoHolder setParameter(double p, double d, double i){
            this.p = p;
            this.d = d;
            this.i = i;
            return this;
        }

        public controllerInfoHolder setParameter(pid param){
            p = param.p();
            i = param.i();
            d = param.d();
            return this;
        }

        public pid getPIDParams(){
            return new pid(p, i, d);
        }


        public double getTargetAngle() {
            return targetAngle;
        }
    }
}
