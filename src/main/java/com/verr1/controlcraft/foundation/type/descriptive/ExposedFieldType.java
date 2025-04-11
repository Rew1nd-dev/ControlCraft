package com.verr1.controlcraft.foundation.type.descriptive;

import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.verr1.controlcraft.utils.ComponentUtils.literals;

public enum ExposedFieldType implements Descriptive<ExposedFieldType> {

    NONE(false),
    P(false, Component.literal("P"),
            literals(
                    "Proportional Ratio",
                    "A High Value Will:",
                    " . +Response",
                    " . +Oscillation",
                    "Suggestions:",
                    " . Always Positive",
                    " . For Position: 24",
                    " . For Speed: 10"
            )
    ),
    I(false, Component.literal("I"),
            literals(
                    "Integral Ratio",
                    "A High Value Will:",
                    " . -Steady-State Error",
                    " . +Oscillation",
                    " . +Dumb For Changes",
                    "Suggestions:",
                    " . 0 For Most Cases"
            )
    ),
    D(false, Component.literal("D"),
            literals(
                    "Derivative Ratio",
                    "A Moderate Value Will:",
                    " . -Response",
                    " . -Oscillation",
                    " . +Smoothness",
                    "Suggestions:",
                    " . High Value Causes Oscillation",
                    " . For Position: <20",
                    " . For Velocity: 0"
            )
    ),

    FORCED_TARGET(false,
            Component.literal("Target"),
            literals(
                "Target Value For Control",
                "Absolute"
        )),
    FORCED_TARGET$1(false),

    COMPLIANCE(false),

    TARGET(false,
            Component.literal("Target"),
            literals(
                    "Target Value For Control"
            )
    ),
    TARGET$1(false),
    TARGET$2(false),

    RADIAN(false, Component.literal("Radian"),

            literals(
                    "Angle in rad"
            )
    ),

    DEGREE(false, Component.literal("Degree"),
            literals(
                    "Angle in °"
            )
    ),
    DEGREE$1(false),
    DEGREE$2(false),

    HORIZONTAL_TILT(false, Component.literal("Horizontal"),
            literals(
                    "in rad",
                    "See Rudder Changes For Direction"
            )
    ),
    VERTICAL_TILT(false, Component.literal("Vertical"),
            literals(
                    "in rad",
                    "See Rudder Changes For Direction"
            )
    ),

    HORIZONTAL_TILT$1(false),
    VERTICAL_TILT$1(false),

    SPEED(false),
    SPEED$1(false),

    TORQUE(false),
    FORCE(false),
    THRUST(false),

    IS_LOCKED(true, Component.literal("Is Locked"),
            literals(
                    "If The Device Is Locked By Constraint"
            )
    ),
    IS_LOCKED$1(true),

    OFFSET(false, Component.literal("Offset"),
            literals(
                    "Ship Offset For Next Assembly"
            )
    ),

    ANCHOR_OFFSET(false, Component.literal("Offset"),
            literals(
                    "Anchor Offset Distance",
                    "Only Works For Non-Static One"
            )
    ),

    IS_RUNNING(true, Component.literal("Is Running"),
            literals(
                    "If Spatial Is Active"
            )
    ),
    IS_STATIC(true, Component.literal("Is Static"),
            literals(
                    "If Spatial Is Static:",
                    " . Be Target For Non-Static Spatial",
                    " . No Control Over Ship",
                    "If Spatial Is Non-Static:",
                    " . Looks For Static Spatial",
                    " . Controls Ship If Found"
            )
    ),

    IS_SENSOR(true, Component.literal("Is Sensor"),
            literals(
                    "If Camera Is a distance Sensor:",
                    " . Outputs Redstone By Distance"
            )
    ),

    CAST_RAY(true, Component.literal("Cast Ray"),
            literals(
                    "Whether Cast A Ray Of View"
            )
    ),

    CLIP_SHIP(true, Component.literal("Ship Outline"),
            literals(
                    "Whether Cast A Ray Of View"
            )
    ),

    CLIP_ENTITY(true, Component.literal("Entity Outline"),
            literals(
                    "Whether Cast A Ray Of View"
            )
    ),

    // these are not exposed to the terminal

    STRENGTH(false),
    AIR_RESISTANCE(false, Component.literal("Air Resist"),
            literals(
                    "Air Resistance",
                    " . applied at com",
                    " . Suggest < 0.5",
                    " . Unstable If Too Big"
            )
    ),
    EXTRA_GRAVITY(false, Component.literal("Extra G"),
            literals(
                    "Extra Gravity",
                    " . applied at com",
                    " . -10 to eliminate gravity"
            )
    ),
    ROTATIONAL_RESISTANCE(false, Component.literal("Rot Damp"),
            literals(
                    "Rotational Resistance",
                    " . Suggest < 0.1"
            )
    ),

    PROTOCOL(false, Component.literal("Protocol"),
            literals(
                    "Protocol Of Channel",
                    " . Number Only"
            )
    ),
    NAME(false, Component.literal("Name"),
            literals(
                    "Name Of Device",
                    " . Unique Under Same Channel",
                    " . Not\"null\" ",
                    " . Not\"\"",
                    " . \"null\" if unregistered"
            )
    ),
    TYPE(false, Component.literal("Type"),
            literals(
                    "Peripheral Type"
            )
    ),
    VALUE(false),

    MODE_ANGULAR(false),
    MODE_POSITION(false),
    MODE_SPEED(false),

    MODE_CHEAT(false),

    THRUST_RATIO(false),
    TORQUE_RATIO(false);

    private boolean isBoolean = false;

    ExposedFieldType(boolean isBoolean, Component name, List<Component> descriptions) {
        this.isBoolean = isBoolean;
        LangUtils.registerDefaultName(ExposedFieldType.class, this, name);
        LangUtils.registerDefaultDescription(ExposedFieldType.class, this, descriptions);
    }

    ExposedFieldType(boolean isBoolean) {
        this.isBoolean = isBoolean;
        LangUtils.registerDefaultName(ExposedFieldType.class, this, Component.literal(this.name()));
        LangUtils.registerDefaultDescription(ExposedFieldType.class, this, List.of());
    }


    public boolean isBoolean(){
        return isBoolean;
    }

    public @NotNull Component asComponent(){
        return toMain().callComponentLikeSuper();
    }


    @Override
    public ExposedFieldType self() {
        return this;
    }

    @Override
    public Class<ExposedFieldType> clazz() {
        return ExposedFieldType.class;
    }

    private Component callComponentLikeSuper(){
        return Descriptive.super.asComponent();
    }

    private List<Component> callDescriptiveSuper(){
        return Descriptive.super.specific();
    }

    ExposedFieldType toMain(){
        try{
            String[] main_key = name().split("\\$");
            return ExposedFieldType.valueOf(main_key[0]);
        }catch (Exception e){
            return this;
        }
    }

    @Override
    public List<Component> overall() {
        return Descriptive.super.overall();
    }

    @Override
    public List<Component> specific() {
        return toMain().callDescriptiveSuper();
    }

    public static void register(){
        LangUtils.registerDefaultDescription(ExposedFieldType.class, literals("Some Fields Is Redundant", "In order to apply different Min-Max"));
    }
}
