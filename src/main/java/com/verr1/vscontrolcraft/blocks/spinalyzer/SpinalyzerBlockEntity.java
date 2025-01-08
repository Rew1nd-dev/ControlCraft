package com.verr1.vscontrolcraft.blocks.spinalyzer;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.verr1.vscontrolcraft.compat.cctweaked.peripherals.SpinalyzerPeripheral;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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

public class SpinalyzerBlockEntity extends SmartBlockEntity {


    private BlockPos pairPos;


    private SpinalyzerPeripheral peripheral;
    private LazyOptional<IPeripheral> peripheralCap;

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
        ServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) level, getBlockPos());
        Matrix3d base_wc2sc = ship == null ? getRotationMatrix_World_wc2sc() : getRotationMatrix_Ship_wc2sc(ship);
        Matrix3d extra_sc2pc = getRotationMatrixOfPlacement_sc2pc();
        return new Matrix3d(base_wc2sc).mul(extra_sc2pc);
    }

    public Quaterniondc getQuaternion(){
        ServerShip ship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel) level, getBlockPos());
        if(ship == null)return getQuaternion_World_wc2sc();
        return getQuaternion_Ship_wc2sc(ship);
    }

    public Matrix3d getTransformFromPair(){
        if(pairPos != null && level.getExistingBlockEntity(pairPos) instanceof SpinalyzerBlockEntity){
            var pairBe = (SpinalyzerBlockEntity) level.getExistingBlockEntity(pairPos);
            Matrix3d selfTransform = getRotationMatrix_wc2sc();
            Matrix3d pairTransform = pairBe.getRotationMatrix_wc2sc();
            return new Matrix3d(pairTransform.transpose()).mul(selfTransform);
        }
        return getRotationMatrix_wc2sc();
    }


    public void pairWith(SpinalyzerBlockEntity pair){
        this.pairPos = pair.getBlockPos();
    }

    public double getRotationAngle(){
        Quaterniondc q_pair = new Quaterniond();;
        if(pairPos != null && level.getExistingBlockEntity(pairPos) instanceof SpinalyzerBlockEntity){
            var pairBe = (SpinalyzerBlockEntity) level.getExistingBlockEntity(pairPos);
            q_pair = pairBe.getQuaternion();
        }

        Quaterniondc q_self = getQuaternion();

        return q_pair.conjugate(new Quaterniond()).mul(q_self).angle();
    }

    private Quaterniondc getQuaternion_World_wc2sc(){
        return new Quaterniond(); // Identity Quaternion
    }

    private Quaterniondc getQuaternion_Ship_wc2sc(@NotNull ServerShip ship){
        return ship.getTransform().getShipToWorldRotation();
    }

    private Matrix3d getRotationMatrix_World_wc2sc(){
        return new Matrix3d(); // Identity Matrix
    }

    private Matrix3d getRotationMatrix_Ship_wc2sc(@NotNull ServerShip ship){
        return ship.getTransform().getWorldToShip().get3x3(new Matrix3d());
    }


    public Matrix3d getRotationMatrixOfPlacement_sc2pc(){
        return new Matrix3d();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }
}
