package com.verr1.controlcraft.foundation.type.descriptive;

import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import static com.verr1.controlcraft.utils.ComponentUtils.literals;

public enum GroupPolicy implements Descriptive<GroupPolicy> {
    SUM(
            (allValues, $) -> allValues.stream().reduce(0.0, Double::sum),
            literals(
                    "Sum All Values",
                    "Sum Up Values of All Activated Channels"
            )
    ),
    EXCLUSIVE(
            ($, thisValue) -> thisValue,
            literals(
                    "Exclusive Input",
                    "Only Apply Latest Activated Channel"
            )
    );

    GroupPolicy(
            BiFunction<Collection<Double>, Double, Double> mapping,
            List<Component> description
    ) {
        this.mapping = mapping;
        LangUtils.registerDefaultName(GroupPolicy.class, this, Component.literal(name().toUpperCase()));
        LangUtils.registerDefaultDescription(GroupPolicy.class, this, description);
    }

    public final BiFunction<Collection<Double>, Double, Double> mapping;  // (allValues, thisValue) -> outputValue

    @Override
    public GroupPolicy self() {
        return this;
    }

    @Override
    public Class<GroupPolicy> clazz() {
        return GroupPolicy.class;
    }

    public static void register(){
        LangUtils.registerDefaultDescription(GroupPolicy.class, literals("Decides How To Do With Multiple Channels"));
    }
}
