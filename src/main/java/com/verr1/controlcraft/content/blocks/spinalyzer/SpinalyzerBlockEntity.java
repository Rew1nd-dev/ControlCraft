package com.verr1.controlcraft.content.blocks.spinalyzer;


import com.verr1.controlcraft.content.blocks.OnShipBlockEntity;
import com.verr1.controlcraft.content.cctweaked.peripheral.SpinalyzerPeripheral;
import com.verr1.controlcraft.content.valkyrienskies.attachments.Observer;
import com.verr1.controlcraft.foundation.data.ExpirableListener;
import com.verr1.controlcraft.foundation.data.ShipPhysics;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.util.Optional;

public class SpinalyzerBlockEntity extends OnShipBlockEntity {

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

    public Matrix3dc getRotationMatrix_w2s(){
        return readSelf().rotationMatrix();
    }

    public Matrix3dc getRotationMatrix_s2w(){
        return readSelf().rotationMatrix();
    }

    public Quaterniondc getQuaternion(){
        return readSelf().quaternion();
    }

    public Vector3dc getPosition(){
        return readSelf().position();
    }

    public Vector3dc getVelocity(){
        return readSelf().velocity();
    }

    public Vector3dc getSpinalyzerPosition(){
        Vector3d p_sc = ValkyrienSkies.set(new Vector3d(), getBlockPos().getCenter());
        return Optional
                .ofNullable(getShipOn())
                .map(ship -> ship
                            .getTransform()
                            .getShipToWorld()
                            .transformPosition(p_sc)
                )
                .orElse(p_sc);
    }

    public Vector3dc getSpinalyzerVelocity(){
        return Optional
                    .ofNullable(getShipOn())
                    .map(ship ->
                    {
                        ShipPhysics p = readSelf();
                        Vector3dc sv_wc = p.velocity();
                        Vector3dc sw_wc = p.omega();

                        Vector3dc s_sc = p.positionInShip();
                        Vector3dc p_sc = ValkyrienSkies.set(new Vector3d(), getBlockPos().getCenter());
                        Vector3dc r_sc = new Vector3d(p_sc).sub(s_sc);

                        Vector3dc r_wc = p.s2wTransform().transformDirection(r_sc, new Vector3d());
                        return new Vector3d(sv_wc).add(new Vector3d(sw_wc).cross(r_wc));
                    })
                    .orElse(new Vector3d());

    }

    public Vector3dc getAngularVelocity(){
        return readSelf().omega();
    }



    @Override
    public void lazyTickServer() {
        super.lazyTickServer();
        syncAttachInducer();
    }

    public void syncAttachInducer(){
        if(level != null && level.isClientSide)return;
        Optional
            .ofNullable(getLoadedServerShip())
            .map(Observer::getOrCreate)
            .ifPresent(inducer -> inducer.replace(
                WorldBlockPos.of(level, getBlockPos()),
                new ExpirableListener<>(sp -> Optional.ofNullable(peripheral).ifPresent(p -> p.queueEvent(sp)), 60)
            ));
    }
}
