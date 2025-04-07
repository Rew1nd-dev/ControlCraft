package com.verr1.controlcraft.registry;

import com.verr1.controlcraft.ControlCraft;
import net.minecraft.network.chat.Component;

import static net.minecraft.ChatFormatting.GRAY;

public class ControlCraftGuiLabels {
    public static final Component confirmLabel = Component
            .translatable(ControlCraft.MODID + ".screen.labels.confirm")
            .withStyle(GRAY);

    public static final Component cycleLabel = Component
            .translatable(ControlCraft.MODID + ".screen.labels.cycle")
            .withStyle(GRAY);

    public static final Component targetLabel = Component
            .translatable(ControlCraft.MODID + ".screen.labels.target");

    public static final Component valueLabel = Component
            .translatable(ControlCraft.MODID + ".screen.labels.value");

    public static final Component minLabel = Component
            .translatable(ControlCraft.MODID + ".screen.labels.min");


    public static final Component maxLabel = Component
            .translatable(ControlCraft.MODID + ".screen.labels.max");

    public static final Component fieldLabel = Component
            .translatable(ControlCraft.MODID + ".screen.labels.field");

    public static final Component onLabel = Component
            .translatable(ControlCraft.MODID + ".screen.labels.on");

    public static final Component offLabel = Component
            .translatable(ControlCraft.MODID + ".screen.labels.off");

    public static final Component directionLabel = Component
            .translatable(ControlCraft.MODID + ".screen.labels.dir");

    public static final Component cheatLabel = Component
            .translatable(ControlCraft.MODID + ".screen.labels.cheat");

    public static final Component redstoneLabel = Component
            .translatable(ControlCraft.MODID + ".screen.labels.redstone");
}
