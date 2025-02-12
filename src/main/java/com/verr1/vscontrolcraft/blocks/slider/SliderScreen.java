package com.verr1.vscontrolcraft.blocks.slider;

import com.verr1.vscontrolcraft.base.Servo.PIDControllerScreen;
import net.minecraft.core.BlockPos;

public class SliderScreen extends PIDControllerScreen {
    public SliderScreen(BlockPos entityPos, double p, double i, double d, double v, double t) {
        super(entityPos, p, i, d, v, t);
    }

    @Override
    public void initWidgets() {
        super.initWidgets();
        cycleMode.visible = false;
    }
}
