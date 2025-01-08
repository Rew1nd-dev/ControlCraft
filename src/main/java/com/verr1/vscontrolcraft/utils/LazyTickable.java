package com.verr1.vscontrolcraft.utils;

import net.minecraft.client.renderer.texture.Tickable;

public interface LazyTickable extends Tickable {
    void lazyTick();
}
