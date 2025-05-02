package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.verr1.controlcraft.foundation.data.WorldBlockPos;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ExpirableForceInducer<T> implements ShipForcesInducer {
    @JsonIgnore
    private final ConcurrentHashMap<WorldBlockPos, ExpirableControlContext<T>> lives = new ConcurrentHashMap<>();
    @JsonIgnore
    private final int lazyTickRate = 30;
    @JsonIgnore
    private int lazyTickCount = lazyTickRate;

    @Override
    public final void applyForces(@NotNull PhysShip physShip) {
        lazyTickLives();
        applyControl(physShip);
    }

    @Override
    public final void applyForcesAndLookupPhysShips(
            @NotNull PhysShip physShip,
            @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip
    ) {
        applyControlWithOther(physShip, lookupPhysShip);
    }


    public void replace(
            WorldBlockPos pos,
            Supplier<T> provider
    ) {
        lives.put(pos, new ExpirableControlContext<>(provider));
    }

    protected void applyControl(@NotNull PhysShip physShip){};

    protected void applyControlWithOther(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip){
        lives
            .values()
            .stream()
            .map(ExpirableControlContext::context)
            .filter(Objects::nonNull)
            .forEach(
                context -> consume(physShip, lookupPhysShip, context)
            );
    };

    protected abstract void consume(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip, @NotNull T context);




    public void tickActivated(){
        lives.values().forEach(ExpirableControlContext::tick);
        lives.entrySet().removeIf(e -> e.getValue().expired());
    }

    protected void lazyTickLives(){
        if(--lazyTickCount > 0){
            return;
        }
        lazyTickCount = lazyTickRate;
        tickActivated();
    }




}
