package com.verr1.controlcraft.foundation.data.render;

import com.simibubi.create.foundation.outliner.LineOutline;
import com.simibubi.create.foundation.outliner.Outline;
import com.simibubi.create.foundation.utility.Color;
import net.minecraft.world.phys.Vec3;

public class RayLerpHelper {

    public Line LastTickLine = Line.EMPTY;
    public Line CurrentTickLine = Line.EMPTY;

    public TransparentLineOutline renderLine = new TransparentLineOutline();

    public boolean shouldRender = false;

    public int live = 10;

    public Vec3 lerp(float partialTicks, Vec3 start, Vec3 end) {
        return start.add(end.subtract(start).scale(partialTicks));
    }

    public Line lerp(float partialTicks) {
        Vec3 start = lerp(partialTicks, LastTickLine.start(), CurrentTickLine.start());
        Vec3 end = lerp(partialTicks, LastTickLine.end(), CurrentTickLine.end());
        return new Line(start, end);
    }

    public void push(Line newLine){
        activate();
        LastTickLine = CurrentTickLine;
        CurrentTickLine = newLine;
    }

    public void activate(){
        live = 10;
        shouldRender = true;
    }

    public void tick(){
        if(live > 0){
            live--;
        }else{
            shouldRender = false;
        }
    }

    public void constructLine(float pt){
        Line lerped = lerp(pt);
        renderLine.set(lerped.start(), lerped.end());
    }

    public Outline.OutlineParams outlineParams(){
        return renderLine.getParams();
    }

}
