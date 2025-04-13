package com.verr1.controlcraft.foundation.type.descriptive;

import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.verr1.controlcraft.utils.ComponentUtils.literals;

public enum UIContents implements Descriptive<UIContents> {
    CURRENT(Component.literal("Current"), literals("Current Angle, Velocity, Position Etc.")),
    LOCKED(Component.literal("Locked"), literals("Whether The Device Is Locked By Constraint")),
    TARGET(Component.literal("Target"), literals("Target Angle, Velocity, Position Etc.")),
    SELF_OFFSET(Component.literal("Offset"), literals("Rotation Axis Offset For Next Assembly / Connection")),
    COMP_OFFSET(Component.literal("Offset"), literals("Companion Offset For Next Assembly / Connection")),

    MODE(Component.literal("Mode"), literals("Velocity / Position")),
    CHEAT(Component.literal("Cheat"), literals("Convenience")),
    AUTO_LOCK(Component.literal("Auto Lock"), literals("Locked When:", " .Target Speed = 0", " .Target Angle Reached")),
    PID_CONTROLLER(Component.literal("PID Controller"), literals("Integrated Proportional Integral Derivative Controller")),
    QPID_CONTROLLER(Component.literal("Rotation"), literals("PID For Rotation")),
    PPID_CONTROLLER(Component.literal("Position"), literals("PID For Position")),

    COMPLIANCE(Component.literal("Compliance"), literals("actual value = 10 ^ (ui value)")),

    MIN(Component.literal("Min"), literals("Minimum Value Of Signal 0")),
    MAX(Component.literal("Max"), literals("Maximum Value Of Signal 15")),

    TYPE(Component.literal("Type"), literals("Type Of The Peripheral")),
    PROTOCOL(Component.literal("Protocol"), literals("Unique Channel")),
    NAME(Component.literal("Name"), literals("Unique Name Under A Same Protocol")),
    SPATIAL_OFFSET(Component.literal("Spatial Offset"), literals("Offset Distance In Space")),

    ANCHOR_RESISTANCE_AT_POS(Component.literal("Resist At Pos"), literals("Resistance Apply To Block Instead Of COM")),
    ANCHOR_EXTRA_GRAVITY_AT_POS(Component.literal("Gravity At Pos"), literals("Extra Gravity Apply To Block Instead Of COM")),

    CAMERA_LINK_ACCEPT(Component.literal("Camera Link"), literals("Link To Camera")),
    CAMERA_LINK_DUMP(Component.literal("Camera Dump"), literals("Dump Camera Link")),
    CAMERA_LINK_RESET(Component.literal("Camera Reset"), literals("Dump All Camera Link")),
    CAMERA_LINK_VALIDATE(Component.literal("Camera Validate"), literals("Dump Unloaded Or Removed Camera Link")),
    ;


    public FormattedLabel toUILabel() {
        var l = new FormattedLabel(0, 0, asComponent());
        l.setText(asComponent());
        return l;
    }



    UIContents(Component displayName, List<Component> description) {
        LangUtils.registerDefaultName(UIContents.class, this, displayName);
        LangUtils.registerDefaultDescription(UIContents.class, this, description);

    }

    @Override
    public UIContents self() {
        return this;
    }

    @Override
    public Class<UIContents> clazz() {
        return UIContents.class;
    }

    public static void register(){
        LangUtils.registerDefaultDescription(UIContents.class, literals("UI Contents"));
    }
}
