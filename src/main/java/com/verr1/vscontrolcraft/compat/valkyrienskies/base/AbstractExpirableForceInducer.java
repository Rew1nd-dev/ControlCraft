package com.verr1.vscontrolcraft.compat.valkyrienskies.base;

import com.verr1.vscontrolcraft.base.DataStructure.LevelPos;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractExpirableForceInducer implements ShipForcesInducer {
    private final ConcurrentHashMap<LevelPos, Integer> lives = new ConcurrentHashMap<>();
    private final int lazyTickRate = 30;
    private int lazyTickCount = lazyTickRate;

    @Override
    public abstract void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip);

    public void tickActivated(){
        lives.entrySet().forEach(e -> e.setValue(e.getValue() - 1));
        lives.entrySet().removeIf(e -> e.getValue() < 0);
    }

    protected void lazyTickLives(){
        if(--lazyTickCount > 0){
            return;
        }
        lazyTickCount = lazyTickRate;
        tickActivated();
    }


    protected ConcurrentHashMap<LevelPos, Integer> getLives(){
        return lives;
    }

    public void update(LevelPos pos){
        lives.put(pos, 10);
    }

    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        lazyTickLives();
    }



}
