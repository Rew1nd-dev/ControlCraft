package com.verr1.controlcraft.content.cctweaked.peripheral;


import com.google.common.collect.Sets;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@ParametersAreNonnullByDefault
public abstract class AbstractAttachedPeripheral<T extends BlockEntity> implements IPeripheral {
    private final T target;
    private final Set<IComputerAccess> computers = Sets.newConcurrentHashSet();

    @Override
    public abstract String getType();

    public AbstractAttachedPeripheral(T target) {
        this.target = target;
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
    }

    @Override
    public @NotNull T getTarget(){
        return target;
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
    }

    @Override
    public abstract boolean equals(@Nullable IPeripheral iPeripheral);
}
