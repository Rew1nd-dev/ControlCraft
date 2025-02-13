package com.verr1.vscontrolcraft.blocks.slider;

import com.verr1.vscontrolcraft.base.Servo.PIDControllerScreen;
import com.verr1.vscontrolcraft.registry.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class SliderScreen extends PIDControllerScreen {
    public SliderScreen(BlockPos entityPos, double p, double i, double d, double v, double t) {
        super(entityPos, p, i, d, v, t);
    }

    @Override
    public void initWidgets() {
        super.initWidgets();
        cycleMode.visible = false;
    }

    @Override
    protected ItemStack renderedItem() {
        return AllBlocks.SLIDER_CONTROLLER_BLOCK.asStack();
    }
}
