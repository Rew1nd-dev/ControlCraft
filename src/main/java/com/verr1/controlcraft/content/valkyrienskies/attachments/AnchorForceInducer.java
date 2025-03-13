package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.verr1.controlcraft.content.blocks.anchor.AnchorBlockEntity;
import com.verr1.controlcraft.content.valkyrienskies.controls.InducerControls;
import com.verr1.controlcraft.foundation.ServerBlockEntityGetter;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.attachment.AttachmentHolder;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.concurrent.ConcurrentHashMap;


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
    public void applyForces(@NotNull PhysShip physShip) {
        lazyTickLives();
        applyControls(physShip);
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip){
        applyControlsWithOther(physShip, lookupPhysShip);
    }

//  Super Class End Here. Currently, No Able to use inherited class

    public static AnchorForceInducer getOrCreate(AttachmentHolder ship){
        //return ship.getOrPutAttachment(AnchorForceInducer.class, AnchorForceInducer::new);
        var obj = ship.getAttachment(AnchorForceInducer.class);
        if(obj == null){
            obj = new AnchorForceInducer();
            ship.setAttachment(obj);
        }
        return obj;

    }



    public void applyControlsWithOther(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {

    }



    public void applyControls(@NotNull PhysShip physShip) {
        getLives()
            .forEach(
                (pos, live) ->
                    ServerBlockEntityGetter.INSTANCE
                    .getBlockEntityAt(pos.globalPos(), AnchorBlockEntity.class)
                    .ifPresent(anchor -> InducerControls.anchorTickControls(
                            anchor.getLogicalAnchor(),
                            physShip
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