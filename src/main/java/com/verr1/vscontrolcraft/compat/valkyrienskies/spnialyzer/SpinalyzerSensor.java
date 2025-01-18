package com.verr1.vscontrolcraft.compat.valkyrienskies.spnialyzer;

import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerBlockEntity;

import com.verr1.vscontrolcraft.utils.VSMathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

import java.util.concurrent.ConcurrentHashMap;

public class SpinalyzerSensor implements ShipForcesInducer {
    private final int lazyTickRate = 30;
    private int lazyTickCount = 0;
    private ConcurrentHashMap<LogicalSensor, Integer> spinalsOnShip = new ConcurrentHashMap<>();

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        lazyTick();
        ShipPhysics tickPhysics = VSMathUtils.getShipPhysics((PhysShipImpl) physShip);
        spinalsOnShip.entrySet().forEach(e->{
            BlockPos pos = e.getKey().pos();
            ServerLevel level = e.getKey().level();
            if(level.getExistingBlockEntity(pos) instanceof SpinalyzerBlockEntity spinal){
                spinal.writePhysicsShipInfo(tickPhysics);
            }
        });
    }

    public void updateSensor(LogicalSensor sensor){
        spinalsOnShip.put(sensor, 1);
    }

    public void removeSensor(LogicalSensor sensor){
        spinalsOnShip.remove(sensor);
    }

    public void removeInvalidControllers(){
        spinalsOnShip.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey().pos();
            ServerLevel level = entry.getKey().level();
            return level.getExistingBlockEntity(pos) instanceof SpinalyzerBlockEntity;
        });
    }

    public void lazyTick(){
        if(lazyTickCount-- > 0)return;
        lazyTickCount = lazyTickRate;
        removeInvalidControllers();
    }

    public static SpinalyzerSensor getOrCreate(@NotNull ServerShip ship) {
        SpinalyzerSensor obj = ship.getAttachment(SpinalyzerSensor.class);
        if(obj == null) {
            obj = new SpinalyzerSensor();
            ship.saveAttachment(SpinalyzerSensor.class, obj);
        }
        return obj;
    }


}
