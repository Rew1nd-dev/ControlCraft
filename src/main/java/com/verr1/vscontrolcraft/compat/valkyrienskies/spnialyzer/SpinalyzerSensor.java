package com.verr1.vscontrolcraft.compat.valkyrienskies.spnialyzer;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import com.verr1.vscontrolcraft.blocks.spinalyzer.ShipPhysics;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerBlockEntity;

import com.verr1.vscontrolcraft.compat.valkyrienskies.base.AbstractExpirableForceInducer;
import com.verr1.vscontrolcraft.utils.VSMathUtils;
import kotlin.jvm.functions.Function1;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;

public class SpinalyzerSensor extends AbstractExpirableForceInducer {

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        super.applyForces(physShip);
        ShipPhysics tickPhysics = VSMathUtils.getShipPhysics((PhysShipImpl) physShip);
        getLives().entrySet().forEach(e->{
            BlockPos pos = e.getKey().pos();
            ServerLevel level = e.getKey().level();
            if(level.getExistingBlockEntity(pos) instanceof SpinalyzerBlockEntity spinal){
                spinal.physics.write(tickPhysics);
            }
        });
    }


    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {

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
