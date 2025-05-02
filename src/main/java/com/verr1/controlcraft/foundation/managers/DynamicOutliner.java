package com.verr1.controlcraft.foundation.managers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.outliner.LineOutline;
import com.simibubi.create.foundation.outliner.Outline;
import com.simibubi.create.foundation.outliner.Outliner;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.Color;
import com.verr1.controlcraft.foundation.data.render.Line;
import com.verr1.controlcraft.foundation.data.render.RayLerpHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DynamicOutliner {

    private final Map<Object, OutlineEntry> outlines = Collections.synchronizedMap(new HashMap<>());

    public void tickOutlines() {
        Iterator<OutlineEntry> iterator = outlines.values()
                .iterator();
        while (iterator.hasNext()) {
            OutlineEntry entry = iterator.next();
            entry.tick();
            if (!entry.isAlive())
                iterator.remove();
        }
    }

    public Outline.OutlineParams showLine(Object slot, Vec3 start, Vec3 end) {
        OutlineEntry entry = outlines.computeIfAbsent(slot, k -> new OutlineEntry(new RayLerpHelper()));
        entry.ticksTillRemoval = 1;
        entry.outline.push(new Line(start, end));
        return entry.outline.outlineParams();
    }

    public void renderOutlines(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, float pt) {
        outlines.forEach((key, entry) -> {
            RayLerpHelper outline = entry.getOutline();
            outline.constructLine(pt);
            outline.renderLine.render(ms, buffer, camera, pt);
        });
    }

    public static class OutlineEntry{
        public static final int FADE_TICKS = 8;

        private final RayLerpHelper outline;
        private int ticksTillRemoval = 1;

        public OutlineEntry(RayLerpHelper outline) {
            this.outline = outline;
        }

        public RayLerpHelper getOutline() {
            return outline;
        }

        public int getTicksTillRemoval() {
            return ticksTillRemoval;
        }

        public boolean isAlive() {
            return ticksTillRemoval >= -FADE_TICKS;
        }

        public boolean isFading() {
            return ticksTillRemoval < 0;
        }

        public void tick() {
            ticksTillRemoval--;
            outline.tick();
        }
    }
}
