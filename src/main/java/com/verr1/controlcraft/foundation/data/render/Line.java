package com.verr1.controlcraft.foundation.data.render;

import net.minecraft.world.phys.Vec3;

public record Line(Vec3 start, Vec3 end) {
    public static Line EMPTY = new Line(new Vec3(0, 0, 0), new Vec3(0, 0, 0));
}
