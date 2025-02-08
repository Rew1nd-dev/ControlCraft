package com.verr1.vscontrolcraft.base.Servo;

import com.verr1.vscontrolcraft.registry.AllBlocks;
import net.minecraft.world.item.ItemStack;

public enum PIDControllerType {
    SLIDER,
    SERVO,
    JOINT;


    public ItemStack asItem(){
        return switch (this) {
            case SLIDER -> AllBlocks.SLIDER_CONTROLLER_BLOCK.asStack();
            case SERVO -> AllBlocks.SERVO_MOTOR_BLOCK.asStack();
            case JOINT -> AllBlocks.JOINT_MOTOR_BLOCK.asStack();
        };
    }
}
