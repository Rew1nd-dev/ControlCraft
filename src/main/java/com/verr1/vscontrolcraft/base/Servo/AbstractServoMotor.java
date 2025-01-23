package com.verr1.vscontrolcraft.base.Servo;


import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.base.DataStructure.SynchronizedField;
import com.verr1.vscontrolcraft.base.DeferralExecutor.DeferralExecutor;
import com.verr1.vscontrolcraft.base.ShipConnectorBlockEntity;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.ServoMotorPeripheral;
import com.verr1.vscontrolcraft.compat.valkyrienskies.generic.QueueForceInducer;
import com.verr1.vscontrolcraft.compat.valkyrienskies.servo.LogicalServoMotor;
import com.verr1.vscontrolcraft.compat.valkyrienskies.servo.ServoMotorForceInducer;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.utils.VSConstrainSerializeUtils;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
import java.util.Arrays;
import java.util.Objects;

public abstract class AbstractServoMotor extends ShipConnectorBlockEntity {
    protected ServoMotorPeripheral peripheral;
    protected LazyOptional<IPeripheral> peripheralCap;

    // the assembled face direction facing servo motor of this ship

    public SynchronizedField<ShipPhysics> ownPhysics = new SynchronizedField<>(ShipPhysics.EMPTY);
    public SynchronizedField<ShipPhysics> asmPhysics = new SynchronizedField<>(ShipPhysics.EMPTY);


    protected VSHingeOrientationConstraint savedHinge = null;
    protected VSAttachmentConstraint savedAttach_1 = null;
    protected VSAttachmentConstraint savedAttach_2 = null;
    protected Object Hinge_ID_1;
    protected Object Attach_ID_1;
    protected Object Attach_ID_2;

    private final ControllerInfoHolder servoController = new ControllerInfoHolder();

    public ControllerInfoHolder getControllerInfoHolder(){
        return servoController;
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

    public AbstractServoMotor(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract Direction getServoDirection();

    public abstract Vector3d getServoDirectionJOML();

    public abstract BlockPos getAssembleBlockPos();

    public abstract Vector3d getAssembleBlockPosJOML(); // when shipified, where should the assembled block stay? this could be double vec3

    public Quaterniondc getQuaternionOfPlacement(){
        return VSMathUtils.getQuaternionOfPlacement(getServoDirection());
    }


    public double getServoAngle(){
        Matrix3dc own = ownPhysics.read().rotationMatrix().transpose(new Matrix3d()); //wc2sc
        Matrix3dc asm = asmPhysics.read().rotationMatrix().transpose(new Matrix3d());
        if(getCompanionShipDirection() == null)return 0;
        return -VSMathUtils.get_xc2yc(own, asm, getServoDirection(), getCompanionShipDirection());
    }


    public void applyTorque(double torque){
        ServerShip asm = getCompanionServerShip();
        ServerShip own = getServerShipOn();
        if(asm == null)return;
        if(own == null)return;
        QueueForceInducer qfi_asm = QueueForceInducer.getOrCreate(asm);
        QueueForceInducer qfi_own = QueueForceInducer.getOrCreate(own);
        Vector3d torque_sc = Util.Vec3itoVector3d(getServoDirection().getNormal()).mul(torque);
        Vector3d torque_wc = VSMathUtils.get_sc2wc(own).transform(torque_sc, new Vector3d());
        qfi_asm.applyInvariantTorque(torque_wc);
        qfi_own.applyInvariantTorque(torque_wc.mul(-1));
    }

    public void destroy() {
        super.destroy();
        if(level.isClientSide)return;
        destroyConstrains();

    }

    public void destroyConstrains(){
        if(savedHinge == null || savedAttach_1 == null || savedAttach_2 == null)return;
        var shipWorldCore = (ShipObjectServerWorld)VSGameUtilsKt.getShipObjectWorld(level);
        try{
            shipWorldCore.removeConstraint((int)Hinge_ID_1);
            shipWorldCore.removeConstraint((int)Attach_ID_1);
            shipWorldCore.removeConstraint((int)Attach_ID_2);
            savedHinge = null;
            savedAttach_1 = null;
            savedAttach_2 = null;
            Hinge_ID_1 = null;
            Attach_ID_1 = null;
            Attach_ID_2 = null;
        }catch (Exception e){
            ControlCraft.LOGGER.info(Arrays.toString(e.getStackTrace()));
        }
    }

    public void recreateConstrains(
            VSHingeOrientationConstraint hinge,
            VSAttachmentConstraint attach_1,
            VSAttachmentConstraint attach_2)
    {
        savedHinge = hinge;
        savedAttach_1 = attach_1;
        savedAttach_2 = attach_2;
        recreateConstrains();
    }


    public void recreateConstrains(){
        if(savedHinge == null || savedAttach_1 == null || savedAttach_2 == null)return;
        if(level.isClientSide)return;
        var shipWorldCore = (ShipObjectServerWorld)VSGameUtilsKt.getShipObjectWorld((ServerLevel) level);
        Hinge_ID_1 =  shipWorldCore.createNewConstraint(savedHinge);
        Attach_ID_1 =  shipWorldCore.createNewConstraint(savedAttach_1);
        Attach_ID_2 =  shipWorldCore.createNewConstraint(savedAttach_2);

        if(Hinge_ID_1 == null || Attach_ID_1 == null || Attach_ID_2 == null){
            savedHinge = null;
            savedAttach_1 = null;
            savedAttach_2 = null;
            Hinge_ID_1 = null;
            Attach_ID_1 = null;
            Attach_ID_2 = null;
        }


    }

    public void writeSavedConstrains(CompoundTag tag){
        tag.putBoolean("assembled", getCompanionServerShip() != null);
        if(savedHinge == null || savedAttach_1 == null || savedAttach_2 == null)return;
        tag.putString("assemDir", getCompanionShipDirection().getSerializedName());
        tag.putLong("asm", getCompanionShipID());
        tag.putLong("own", getServerShipID());
        VSConstrainSerializeUtils.writeVSAttachmentConstrain(tag, "attach_1", savedAttach_1);
        VSConstrainSerializeUtils.writeVSAttachmentConstrain(tag, "attach_2", savedAttach_2);
        VSConstrainSerializeUtils.writeVSHingeOrientationConstrain(tag, "hinge", savedHinge);

    }


    public void readSavedConstrains(CompoundTag tag){
        boolean assembled = tag.getBoolean("assembled");
        if(!assembled)return;
        String assemDirString = tag.getString("assemDir");
        Direction assemDir = Direction.byName(assemDirString);
        long asm = tag.getLong("asm");
        long own = tag.getLong("own");

        savedAttach_1 = VSConstrainSerializeUtils.readVSAttachmentConstrain(tag, "attach_1");
        savedAttach_2 = VSConstrainSerializeUtils.readVSAttachmentConstrain(tag, "attach_2");
        savedHinge = VSConstrainSerializeUtils.readVSHingeOrientationConstrain(tag, "hinge");

        setCompanionShipID(asm);
        setCompanionShipDirection(assemDir);
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

    public void syncAssemAttachInducer(){
        if(level.isClientSide)return;
        ServerShip ship = getCompanionServerShip();
        if(ship == null)return;
        var inducer = ServoMotorForceInducer.getOrCreate(ship);
        inducer.updateLogicalServoMotor(
                getBlockPos(),
                new LogicalServoMotor(
                        getServerShipID(),
                        getCompanionShipID(),
                        (ServerLevel) level,
                        getServoDirection(),
                        getCompanionShipDirection()
                )
        );
    }

    public void bruteDirectionalConnectWith(BlockPos assemPos, Direction assemDir){
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

        Vector3dc asmPos_Own = getAssembleBlockPosJOML();
        Vector3dc asmPos_Asm = Util.Vec3toVector3d(assemPos.getCenter());

        VSAttachmentConstraint attachment_1 = new VSAttachmentConstraint(
                ownerShipID,
                assemShipID,
                1.0E-10,
                new Vector3d(asmPos_Own).fma(1, ownDir),
                new Vector3d(asmPos_Asm).fma(-1, asmDir), // This is the opposite with the case of assemble()
                1.0E10,
                0.0
        );

        VSAttachmentConstraint attachment_2 = new VSAttachmentConstraint(
                ownerShipID,
                assemShipID,
                1.0E-10,
                new Vector3d(asmPos_Own).fma(-1, ownDir),
                new Vector3d(asmPos_Asm).fma(1, asmDir), // This is the opposite with the case of assemble()
                1.0E10,
                0.0
        );

        recreateConstrains(hingeConstraint, attachment_1, attachment_2);
        setCompanionShipID(assemShipID);
        setCompanionShipDirection(assemDir);
        notifyUpdate();

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
        Vector3d direction = getServoDirectionJOML();


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

        Vector3dc asmPos_Own = getAssembleBlockPosJOML();
        Vector3dc asmPos_Asm = assembledShip.getInertiaData().getCenterOfMassInShip().add(new Vector3d(0.5, 0.5, 0.5), new Vector3d());

        VSAttachmentConstraint attachment_1 = new VSAttachmentConstraint(
                ownerShipID,
                assembledShipID,
                1.0E-10,
                new Vector3d(asmPos_Own).fma(1, direction),
                new Vector3d(asmPos_Asm).fma(1, direction),
                1.0E10,
                0.0
        );

        VSAttachmentConstraint attachment_2 = new VSAttachmentConstraint(
                ownerShipID,
                assembledShipID,
                1.0E-10,
                new Vector3d(asmPos_Own).fma(-1, direction),
                new Vector3d(asmPos_Asm).fma(-1, direction),
                1.0E10,
                0.0
        );



        recreateConstrains(hingeConstraint, attachment_1, attachment_2);
        setCompanionShipID(assembledShipID);
        setCompanionShipDirection(getServoDirection().getOpposite());
        notifyUpdate();

    }

}
