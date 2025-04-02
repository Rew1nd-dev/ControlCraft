package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.verr1.controlcraft.content.blocks.anchor.AnchorBlockEntity;
import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.vsapi.PhysShipWrapper;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.core.impl.game.ships.PhysShipImpl;


@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class AnchorForceInducer extends AbstractExpirableForceInducer
        implements ShipForcesInducer
{
    

    @Override
    public void applyForces(@NotNull org.valkyrienskies.core.api.ships.PhysShip physShip) {
        lazyTickLives();
        applyControls(physShip);
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull org.valkyrienskies.core.api.ships.PhysShip physShip, @NotNull Function1<? super Long, ? extends org.valkyrienskies.core.api.ships.PhysShip> lookupPhysShip){
        applyControlsWithOther(physShip, lookupPhysShip);
    }

//  Super Class End Here. Currently, No Able to use inherited class

    public static AnchorForceInducer getOrCreate(ServerShip ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(AnchorForceInducer.class);
        if(obj == null){
            obj = new AnchorForceInducer();
            ship.saveAttachment(AnchorForceInducer.class, obj);
        }
        return obj;

    }



    public void applyControlsWithOther(@NotNull org.valkyrienskies.core.api.ships.PhysShip physShip, @NotNull Function1<? super Long, ? extends org.valkyrienskies.core.api.ships.PhysShip> lookupPhysShip) {

    }



    public void applyControls(@NotNull org.valkyrienskies.core.api.ships.PhysShip physShip) {
        getLives()
            .forEach(
                (pos, live) ->
                    BlockEntityGetter.INSTANCE
                    .getBlockEntityAt(pos.globalPos(), AnchorBlockEntity.class)
                    .ifPresent(anchor -> InducerControls.anchorTickControls(
                            anchor.getLogicalAnchor(),
                            new PhysShipWrapper((PhysShipImpl) physShip)
                    )

                ));
    }



}

/*
* @JsonIgnore
    private final ConcurrentHashMap<WorldBlockPos, Integer> lives = new ConcurrentHashMap<>();
    private final int lazyTickRate = 30;
    private int lazyTickCount = lazyTickRate;

    public void tickActivated(){
        lives.entrySet().forEach(e -> e.setValue(e.getValue() - 1));
        lives.entrySet().removeIf(e -> e.getValue() < 0);
    }

    private void lazyTickLives(){
        if(--lazyTickCount > 0){
            return;
        }
        lazyTickCount = lazyTickRate;
        tickActivated();
    }


    private ConcurrentHashMap<WorldBlockPos, Integer> getLives(){
        return lives;
    }

    public void alive(WorldBlockPos pos){
        lives.put(pos, 10);
    }
*
* */