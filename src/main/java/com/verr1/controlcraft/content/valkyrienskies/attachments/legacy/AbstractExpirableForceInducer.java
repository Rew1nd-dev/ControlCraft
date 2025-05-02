package com.verr1.controlcraft.content.valkyrienskies.attachments.legacy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;

import java.util.concurrent.ConcurrentHashMap;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractExpirableForceInducer{
    @JsonIgnore
    private final ConcurrentHashMap<WorldBlockPos, Integer> lives = new ConcurrentHashMap<>();
    private final int lazyTickRate = 30;
    private int lazyTickCount = lazyTickRate;






    public abstract void applyControls(@NotNull PhysShip physShip);

    public abstract void applyControlsWithOther(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip);

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


    protected ConcurrentHashMap<WorldBlockPos, Integer> getLives(){
        return lives;
    }

    public void alive(WorldBlockPos pos){
        lives.put(pos, 10);
    }



}
