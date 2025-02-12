package com.verr1.vscontrolcraft.blocks.spinalyzer;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.base.DataStructure.SynchronizedField;
import com.verr1.vscontrolcraft.base.OnShipDirectinonalBlockEntity;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.SpinalyzerPeripheral;
import com.verr1.vscontrolcraft.compat.valkyrienskies.spnialyzer.LogicalSensor;
import com.verr1.vscontrolcraft.compat.valkyrienskies.spnialyzer.SpinalyzerSensor;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.List;

public class SpinalyzerBlockEntity extends OnShipDirectinonalBlockEntity {
    /* TODO:
    *   1. Rewrite getRelativeAngle(), using rotation matrix and reference axis
    *   2. Add checkIfPair Api to peripheral
    *
    */

    private BlockPos source;


    private SpinalyzerPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

    public SynchronizedField<ShipPhysics> physics = new SynchronizedField<>(ShipPhysics.EMPTY);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.CAPABILITY_PERIPHERAL){
            if(this.peripheral == null){
                this.peripheral = new SpinalyzerPeripheral(this);
            }
            if(peripheralCap == null || !peripheralCap.isPresent())
                peripheralCap =  LazyOptional.of(() -> this.peripheral);
            return peripheralCap.cast();
        }
        return super.getCapability(cap, side);
    }

    public SpinalyzerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Matrix3d getRotationMatrix_wc2sc(){
        if(!isOnServerShip())return new Matrix3d();
        return physics.read().s2wTransform().get3x3(new Matrix3d());
    }

    public Quaterniondc getQuaternion(){
        if(!isOnServerShip())return new Quaterniond();
        return physics.read().quaternion();
    }

    public Matrix3d getRelativeSourceTransform(){
        if(source != null && level.getExistingBlockEntity(source) instanceof SpinalyzerBlockEntity pairBe){
            Matrix3d selfTransform = getRotationMatrix_wc2sc();
            Matrix3d pairTransform = pairBe.getRotationMatrix_wc2sc();
            return new Matrix3d(pairTransform.transpose()).mul(selfTransform);
        }
        return getRotationMatrix_wc2sc();
    }



    public void setSource(SpinalyzerBlockEntity pair){
        this.source = pair.getBlockPos();
    }

    public double getRotationAngle(int axis){
        Matrix3d m = getRelativeSourceTransform();
        if(axis == 0){ // rotating around x-axis
            return Math.atan2(m.m21(), m.m22()); // z.y / z.z
        }
        if (axis == 1){ // rotating around y-axis
            return Math.atan2(m.m20(), m.m22()); // z.x / z.z
        }
        if (axis == 2){ // rotating around z-axis
            return Math.atan2(m.m10(), m.m11()); // y.x / y.y
        }
        return 0;
    }


    public void syncAttachedSensor(){
        if(level.isClientSide) return;
        ServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) level, getBlockPos());
        if(ship == null)return;
        var sensor = SpinalyzerSensor.getOrCreate(ship);
        sensor.update(new LevelPos(getBlockPos(), (ServerLevel) getLevel()));
    }




    @Override
    public void lazyTick() {
        super.lazyTick();
        syncAttachedSensor();
    }

}
