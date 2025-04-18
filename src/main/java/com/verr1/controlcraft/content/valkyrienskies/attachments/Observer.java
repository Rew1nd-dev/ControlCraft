package com.verr1.controlcraft.content.valkyrienskies.attachments;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.verr1.controlcraft.foundation.data.*;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Observer implements ShipForcesInducer {
    @JsonIgnore
    private final ConcurrentHashMap<WorldBlockPos, ExpirableListener<ShipPhysics>> Listener = new ConcurrentHashMap<>();
    @JsonIgnore
    private final SynchronizedField<ShipPhysics> Observation = new SynchronizedField<>(ShipPhysics.EMPTY);


    @Override
    public void applyForces(@NotNull PhysShip physShip) {
        ShipPhysics tickPhysics = ShipPhysics.of(physShip);
        Observation.write(tickPhysics);
        Listener.values().forEach(listener -> listener.accept(tickPhysics));
        Listener.values().forEach(ExpirableListener::tick);
        Listener.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    @Override
    public void applyForcesAndLookupPhysShips(@NotNull PhysShip physShip, @NotNull Function1<? super Long, ? extends PhysShip> lookupPhysShip) {

    }


    public static Observer getOrCreate(ServerShip ship){
        var obj = ship.getAttachment(Observer.class);
        if(obj == null){
            obj = new Observer();
            ship.saveAttachment(Observer.class, obj);
        }
        return obj;
    }


    public void replace(WorldBlockPos pos, ExpirableListener<ShipPhysics> listener){
        Listener.put(pos, listener);
        alive(pos);
    }

    private void alive(WorldBlockPos pos){
        Optional.ofNullable(Listener.get(pos)).ifPresent(ExpirableListener::reset);
    }

    public ShipPhysics read(){
        return Observation.read();
    }

}
