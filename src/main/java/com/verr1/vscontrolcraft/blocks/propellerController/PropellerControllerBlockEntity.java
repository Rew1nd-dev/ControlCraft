package com.verr1.vscontrolcraft.blocks.propellerController;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.PropellerControllerPeripheral;
import com.verr1.vscontrolcraft.utils.Util;
import com.verr1.vscontrolcraft.compat.valkyrienskies.propeller.LogicalPropeller;
import com.verr1.vscontrolcraft.compat.valkyrienskies.propeller.PropellerForceInducer;
import com.verr1.vscontrolcraft.blocks.propeller.SimplePropellerBlockEntity;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PropellerControllerBlockEntity extends KineticBlockEntity {
    public boolean hasAttachedPropeller = false;
    public double rotationSpeed = 0;
    public boolean attachPropellerReverseTorque = false;
    public double attachedPropellerThrustRatio = 0;
    public double attachedPropellerTorqueRatio = 0;


    private PropellerControllerPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new PropellerControllerPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidate(){
        super.invalidate();
        if(peripheralCap != null){
            peripheralCap.invalidate();
            peripheralCap = null;
        }
    }

    @Override
    public void tick(){
        super.tick();
        if(level.isClientSide) return;

        //rotationSpeed = 256;

        syncAttachedPropeller();
        syncAttachedInducer();
    }

    public PropellerControllerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public void syncAttachedPropeller(){
        Vec3i direction = this.getBlockState().getValue(BlockStateProperties.FACING).getOpposite().getNormal();
        BlockPos propellerPos = this.getBlockPos().offset(new BlockPos(direction.getX(), direction.getY(), direction.getZ()));
        var attachedBlockEntity = level.getBlockEntity(propellerPos);
        hasAttachedPropeller = attachedBlockEntity instanceof SimplePropellerBlockEntity;
        if(!hasAttachedPropeller)return;
        SimplePropellerBlockEntity propeller = (SimplePropellerBlockEntity) attachedBlockEntity;
        propeller.setVisualRotationalSpeed(rotationSpeed);
        attachedPropellerTorqueRatio = propeller.getTorqueRatio();
        attachedPropellerThrustRatio = propeller.getThrustRatio();
    }

    public boolean canDrive(){
        return hasAttachedPropeller;
    }

    public Vector3d getDirection(){
        return Util.Vec3itoVector3d(getBlockState().getValue(BlockStateProperties.FACING).getNormal());
    }

    public double getTargetSpeed(){
        return rotationSpeed;
    }

    public void setReverseTorque(boolean reverseTorque){
        this.attachPropellerReverseTorque = reverseTorque;
    }

    public void syncAttachedInducer(){
        if(level.isClientSide) return;
        ServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) level, getBlockPos());
        if(ship == null)return;
        var inducer = PropellerForceInducer.getOrCreate(ship);
        if(inducer == null)return;
        inducer.updateLogicalPropeller(
                getBlockPos(),
                new LogicalPropeller(
                        canDrive(),
                        attachPropellerReverseTorque,
                        getDirection(),
                        rotationSpeed,
                        attachedPropellerThrustRatio,
                        attachedPropellerTorqueRatio,
                        (ServerLevel) this.level   //The level is always server sided, because tick() will return at client side
                        )
        );
    }

    public void exitAttachedInducer(){
        if(level.isClientSide) return;
        ServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) level, getBlockPos());
        if(ship == null)return;
        var inducer = PropellerForceInducer.getOrCreate(ship);
        if(inducer == null)return;
        inducer.removeLogicalPropeller(getBlockPos());

    }

    @Override
    public void destroy(){
        exitAttachedInducer();
        super.destroy();
    }

    public void setTargetSpeed(double speed){
        rotationSpeed = speed;
    }

}
