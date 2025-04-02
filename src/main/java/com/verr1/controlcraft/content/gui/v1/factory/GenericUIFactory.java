package com.verr1.controlcraft.content.gui.v1.factory;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.blocks.OptionalSyncedBlockEntity;
import com.verr1.controlcraft.content.blocks.SharedKeys;
import com.verr1.controlcraft.content.blocks.anchor.AnchorBlockEntity;
import com.verr1.controlcraft.content.blocks.camera.CameraBlockEntity;
import com.verr1.controlcraft.content.blocks.flap.FlapBearingBlockEntity;
import com.verr1.controlcraft.content.blocks.jet.JetBlockEntity;
import com.verr1.controlcraft.content.blocks.motor.AbstractDynamicMotor;
import com.verr1.controlcraft.content.blocks.motor.AbstractMotor;
import com.verr1.controlcraft.content.blocks.propeller.PropellerBlockEntity;
import com.verr1.controlcraft.content.blocks.propeller.PropellerControllerBlockEntity;
import com.verr1.controlcraft.content.blocks.receiver.ReceiverBlockEntity;
import com.verr1.controlcraft.content.blocks.slider.DynamicSliderBlockEntity;
import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlockEntity;
import com.verr1.controlcraft.content.gui.v1.layouts.VerticalFlow;
import com.verr1.controlcraft.content.gui.v1.layouts.api.Descriptive;
import com.verr1.controlcraft.content.gui.v1.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.v1.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.v1.layouts.preset.DynamicControllerUIField;
import com.verr1.controlcraft.content.gui.v1.layouts.preset.SpatialScheduleUIField;
import com.verr1.controlcraft.content.gui.v1.layouts.preset.TerminalDeviceUIField;
import com.verr1.controlcraft.content.gui.v1.screens.GenericSettingScreen;
import com.verr1.controlcraft.content.gui.v1.layouts.element.*;
import com.verr1.controlcraft.content.gui.v1.widgets.FormattedLabel;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.foundation.api.IControllerProvider;
import com.verr1.controlcraft.foundation.api.IKinematicUIDevice;
import com.verr1.controlcraft.foundation.api.IScheduleProvider;
import com.verr1.controlcraft.foundation.api.ITerminalDevice;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.data.PeripheralKey;
import com.verr1.controlcraft.foundation.type.descriptive.*;
import com.verr1.controlcraft.registry.ControlCraftBlocks;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import com.verr1.controlcraft.utils.MathUtils;
import com.verr1.controlcraft.utils.ParseUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3d;

import java.util.*;
import java.util.function.UnaryOperator;

import static com.verr1.controlcraft.content.blocks.flap.FlapBearingBlockEntity.ANGLE;
import static com.verr1.controlcraft.foundation.api.ISerializableSchedule.SCHEDULE;

public class GenericUIFactory {
    public static Component NOT_FOUND = Component.literal("Not Found").withStyle(s -> s.withColor(ChatFormatting.RED));

    public static Component lockViewComponent(boolean isLocked){
        return isLocked ?
                Component.literal("Locked").withStyle(s -> s.withColor(ChatFormatting.RED)) :
                Component.literal("Free").withStyle(s -> s.withColor(ChatFormatting.DARK_GREEN));
    }

    public static GenericSettingScreen createAnchorScreen(BlockPos boundAnchorPos){
        DoubleUIField air_resistance_gui = new DoubleUIField(
                d -> boundBlockEntity(boundAnchorPos, AnchorBlockEntity.class).ifPresent(e -> e.setAirResistance(d)),
                () -> boundBlockEntity(boundAnchorPos, AnchorBlockEntity.class).map(AnchorBlockEntity::getAirResistance).orElse(0.0),
                convert(ExposedFieldType.AIR_RESISTANCE, GenericUIFactory::applyCommonTitleStyle)
        );

        DoubleUIField extra_gravity_gui = new DoubleUIField(
                d -> boundBlockEntity(boundAnchorPos, AnchorBlockEntity.class).ifPresent(e -> e.setExtraGravity(d)),
                () -> boundBlockEntity(boundAnchorPos, AnchorBlockEntity.class).map(AnchorBlockEntity::getExtraGravity).orElse(0.0),
                convert(ExposedFieldType.EXTRA_GRAVITY, GenericUIFactory::applyCommonTitleStyle)
        );

        DoubleUIField rotational_resistance_gui = new DoubleUIField(
                d -> boundBlockEntity(boundAnchorPos, AnchorBlockEntity.class).ifPresent(e -> e.setRotationalResistance(d)),
                () -> boundBlockEntity(boundAnchorPos, AnchorBlockEntity.class).map(AnchorBlockEntity::getRotationalResistance).orElse(0.0),
                convert(ExposedFieldType.ROTATIONAL_RESISTANCE, GenericUIFactory::applyCommonTitleStyle)
        );


        BooleanUIField resist_at_pos = new BooleanUIField(
                b -> boundBlockEntity(boundAnchorPos, AnchorBlockEntity.class).ifPresent(e -> e.setAirResistanceAtPos(b)),
                () -> boundBlockEntity(boundAnchorPos, AnchorBlockEntity.class).map(AnchorBlockEntity::isAirResistanceAtPos).orElse(false),
                convert(UIContents.ANCHOR_RESISTANCE_AT_POS, GenericUIFactory::applyCommonTitleStyle)
        );

        BooleanUIField gravity_at_pos = new BooleanUIField(
                b -> boundBlockEntity(boundAnchorPos, AnchorBlockEntity.class).ifPresent(e -> e.setExtraGravityAtPos(b)),
                () -> boundBlockEntity(boundAnchorPos, AnchorBlockEntity.class).map(AnchorBlockEntity::isExtraGravityAtPos).orElse(false),
                convert(UIContents.ANCHOR_EXTRA_GRAVITY_AT_POS, GenericUIFactory::applyCommonTitleStyle)
        );


        alignLabel(air_resistance_gui, extra_gravity_gui, rotational_resistance_gui);
        alignLabel(resist_at_pos, gravity_at_pos);

        return new GenericSettingScreen.builder(boundAnchorPos)
                .withRenderedStack(ControlCraftBlocks.ANCHOR_BLOCK.asStack())
                .withTab(
                        "general",
                        new VerticalFlow.builder(boundAnchorPos)
                                .withPort(AnchorBlockEntity.AIR_RESISTANCE, air_resistance_gui)
                                .withPort(AnchorBlockEntity.EXTRA_GRAVITY, extra_gravity_gui)
                                .withPort(AnchorBlockEntity.ROTATIONAL_RESISTANCE, rotational_resistance_gui)
                                .withPort(AnchorBlockEntity.RESISTANCE_AT_POS, resist_at_pos)
                                .withPort(AnchorBlockEntity.GRAVITY_AT_POS, gravity_at_pos)
                                .build()
                )
                .build();
    }

    public static GenericSettingScreen createCameraScreen(BlockPos boundAnchorPos){
        BooleanUIField is_sensor = new BooleanUIField(
                b -> boundBlockEntity(boundAnchorPos, CameraBlockEntity.class).ifPresent(e -> e.setActiveDistanceSensor(b)),
                () -> boundBlockEntity(boundAnchorPos, CameraBlockEntity.class).map(CameraBlockEntity::isActiveDistanceSensor).orElse(false),
                convert(ExposedFieldType.IS_SENSOR, GenericUIFactory::applyCommonTitleStyle)
        );

        return new GenericSettingScreen.builder(boundAnchorPos)
                .withRenderedStack(ControlCraftBlocks.CAMERA_BLOCK.asStack())
                .withTab(
                        "general",
                        new VerticalFlow.builder(boundAnchorPos)
                                .withPort(CameraBlockEntity.IS_ACTIVE_SENSOR, is_sensor)
                                .build()
                )
                .withTab(
                        "device",
                        createTerminalDeviceTab(boundAnchorPos)
                )
                .build();
    }

    public static GenericSettingScreen createFlapBearingScreen(BlockPos boundPos){
        DoubleUIView angle_view = DoubleUIView.of(
                () -> boundBlockEntity(boundPos, FlapBearingBlockEntity.class).map(be -> String.format("%.4f", be.getAngle())).orElse("Not Found"),
                convert(ExposedFieldType.ANGLE, GenericUIFactory::applyCommonViewStyle)
        );


        DoubleUIField angle = new DoubleUIField(
                b -> boundBlockEntity(boundPos, FlapBearingBlockEntity.class).ifPresent(e -> e.setAngle(b.floatValue())),
                () -> boundBlockEntity(boundPos, FlapBearingBlockEntity.class).map(be -> (double)be.getAngle()).orElse(0.0),
                convert(ExposedFieldType.ANGLE, GenericUIFactory::applyCommonTitleStyle)
        );

        return new GenericSettingScreen.builder(boundPos)
                .withRenderedStack(ControlCraftBlocks.WING_CONTROLLER_BLOCK.asStack())
                .withTab(
                        "general",
                        new VerticalFlow.builder(boundPos)
                                .withPort(SharedKeys.PLACE_HOLDER, angle_view)
                                .withPort(ANGLE, angle)
                                .build()
                )
                .withTab(
                        "device",
                        createTerminalDeviceTab(boundPos)
                )
                .withTickTask(createSyncTasks(boundPos, ANGLE))
                .build();
    }

    public static GenericSettingScreen createPropellerScreen(BlockPos boundPos){
        DoubleUIView speed = DoubleUIView.of(
                () -> boundBlockEntity(boundPos, PropellerBlockEntity.class).map(be -> String.format("%.4f", be.getSpeed())).orElse("Not Found"),
                convert(ExposedFieldType.SPEED, GenericUIFactory::applyCommonViewStyle)
        );


        DoubleUIField torque = new DoubleUIField(
                d -> boundBlockEntity(boundPos, PropellerBlockEntity.class).ifPresent(e -> e.setTorqueRatio(d)),
                () -> boundBlockEntity(boundPos, PropellerBlockEntity.class).map(PropellerBlockEntity::getTorqueRatio).orElse(0.0),
                convert(ExposedFieldType.TORQUE, GenericUIFactory::applyCommonTitleStyle)

        );


        DoubleUIField thrust = new DoubleUIField(
                d -> boundBlockEntity(boundPos, PropellerBlockEntity.class).ifPresent(e -> e.setThrustRatio(d)),
                () -> boundBlockEntity(boundPos, PropellerBlockEntity.class).map(PropellerBlockEntity::getThrustRatio).orElse(0.0),
                convert(ExposedFieldType.THRUST, GenericUIFactory::applyCommonTitleStyle)
        );

        return new GenericSettingScreen.builder(boundPos)
                .withRenderedStack(ControlCraftBlocks.PROPELLER_BLOCK.asStack())
                .withTab(
                        "general",
                        new VerticalFlow.builder(boundPos)
                                .withPort(PropellerBlockEntity.SPEED, speed)
                                .withPort(PropellerBlockEntity.TORQUE, torque)
                                .withPort(PropellerBlockEntity.THRUST, thrust)
                                .build()
                )
                .withTickTask(createSyncTasks(boundPos, PropellerBlockEntity.SPEED))
                .build();

    }


    public static GenericSettingScreen createPropellerControllerScreen(BlockPos boundPos){
        DoubleUIView speed_view = DoubleUIView.of(
                () -> boundBlockEntity(boundPos, PropellerControllerBlockEntity.class).map(be -> String.format("%.4f", be.rotationalSpeed.read())).orElse("Not Found"),
                convert(ExposedFieldType.SPEED, GenericUIFactory::applyCommonViewStyle)
        );

        DoubleUIField speed = new DoubleUIField(
                d -> boundBlockEntity(boundPos, PropellerControllerBlockEntity.class).ifPresent(e -> e.rotationalSpeed.write(d)),
                () -> boundBlockEntity(boundPos, PropellerControllerBlockEntity.class).map(e -> e.rotationalSpeed.read()).orElse(0.0),
                convert(ExposedFieldType.SPEED, GenericUIFactory::applyCommonTitleStyle)
        );

        Runnable alignLabels = () -> alignLabel(speed, speed_view);

        return new GenericSettingScreen.builder(boundPos)
                .withRenderedStack(ControlCraftBlocks.PROPELLER_CONTROLLER.asStack())
                .withTab(
                        "general",
                        new VerticalFlow.builder(boundPos)
                                .withPort(SharedKeys.PLACE_HOLDER, speed_view)
                                .withPort(SharedKeys.VALUE, speed)
                                .withPreDoLayout(alignLabels)
                                .build()
                )
                .withTab(
                        "device",
                        createTerminalDeviceTab(boundPos)
                )
                .withTickTask(createSyncTasks(boundPos, SharedKeys.VALUE))
                .build();

    }

    public static GenericSettingScreen createJetScreen(BlockPos boundPos){
        DoubleUIView thrust_view = DoubleUIView.of(
                () -> boundBlockEntity(boundPos, JetBlockEntity.class).map(be -> String.format("%.4f", be.thrust.read())).orElse("Not Found"),
                convert(ExposedFieldType.THRUST, GenericUIFactory::applyCommonViewStyle)
        );

        DoubleUIView horizontal_view = DoubleUIView.of(
                () -> boundBlockEntity(boundPos, JetBlockEntity.class).map(be -> String.format("%.4f", be.horizontalAngle.read())).orElse("Not Found"),
                convert(ExposedFieldType.HORIZONTAL_TILT, GenericUIFactory::applyCommonViewStyle)
        );

        DoubleUIView vertical_view = DoubleUIView.of(
                () -> boundBlockEntity(boundPos, JetBlockEntity.class).map(be -> String.format("%.4f", be.verticalAngle.read())).orElse("Not Found"),
                convert(ExposedFieldType.VERTICAL_TILT, GenericUIFactory::applyCommonViewStyle)
        );


        DoubleUIField thrust = new DoubleUIField(
                d -> boundBlockEntity(boundPos, JetBlockEntity.class).ifPresent(e -> e.thrust.write(d)),
                () -> boundBlockEntity(boundPos, JetBlockEntity.class).map(e -> e.thrust.read()).orElse(0.0),
                convert(ExposedFieldType.THRUST, GenericUIFactory::applyCommonTitleStyle)
        );

        DoubleUIField horizontal = new DoubleUIField(
                d -> boundBlockEntity(boundPos, JetBlockEntity.class).ifPresent(e -> e.horizontalAngle.write(d)),
                () -> boundBlockEntity(boundPos, JetBlockEntity.class).map(e -> e.horizontalAngle.read()).orElse(0.0),
                convert(ExposedFieldType.HORIZONTAL_TILT, GenericUIFactory::applyCommonTitleStyle)
        );

        DoubleUIField vertical = new DoubleUIField(
                d -> boundBlockEntity(boundPos, JetBlockEntity.class).ifPresent(e -> e.verticalAngle.write(d)),
                () -> boundBlockEntity(boundPos, JetBlockEntity.class).map(e -> e.verticalAngle.read()).orElse(0.0),
                convert(ExposedFieldType.VERTICAL_TILT, GenericUIFactory::applyCommonTitleStyle)
        );
        Runnable alignLabels = () -> alignLabel(thrust, horizontal, vertical);

        return new GenericSettingScreen.builder(boundPos)
                .withRenderedStack(ControlCraftBlocks.JET_BLOCK.asStack())
                .withTab(
                        "general",
                        new VerticalFlow.builder(boundPos)
                                .withPort(SharedKeys.PLACE_HOLDER, thrust_view)
                                .withPort(SharedKeys.PLACE_HOLDER_1, horizontal_view)
                                .withPort(SharedKeys.PLACE_HOLDER_2, vertical_view)
                                .withPort(JetBlockEntity.THRUST, thrust)
                                .withPort(JetBlockEntity.HORIZONTAL_ANGLE, horizontal)
                                .withPort(JetBlockEntity.VERTICAL_ANGLE, vertical)
                                .withPreDoLayout(alignLabels)
                                .build()
                ).withTab(
                        "device",
                        createTerminalDeviceTab(boundPos)
                ).withTickTask(createSyncTasks(boundPos,
                        JetBlockEntity.THRUST,
                        JetBlockEntity.HORIZONTAL_ANGLE,
                        JetBlockEntity.VERTICAL_ANGLE)
                )
                .build();

    }


    public static Style applyCommonViewStyle(Style s){
        return s.withBold(true).withUnderlined(true).withItalic(false).withColor(ChatFormatting.GOLD);
    }

    public static Style applyCommonTitleStyle(Style s){
        return s.withItalic(false).withColor(ChatFormatting.DARK_GRAY);
    }

    public static MutableComponent nameOf(Descriptive<?> d){
        return d.asComponent().copy();
    }

    public static List<MutableComponent> overallOf(Descriptive<?> d){
        return d.overall().stream().map(Component::copy).toList();
    }

    public static List<MutableComponent> specificOf(Descriptive<?> d){
        return d.specific().stream().map(Component::copy).toList();
    }

    public static GenericSettingScreen createDynamicMotorScreen(BlockPos boundPos, ItemStack stack){
        DoubleUIView current_view = DoubleUIView.of(
                () -> boundBlockEntity(boundPos, AbstractDynamicMotor.class).map(be -> String.format("%.4f", be.getController().getValue())).orElse("Not Found"),
                convert(UIContents.CURRENT, GenericUIFactory::applyCommonViewStyle)
        );
        ComponentUIView lock_view = new ComponentUIView(
                () -> boundBlockEntity(boundPos, AbstractDynamicMotor.class).map(be -> lockViewComponent(be.isLocked())).orElse(NOT_FOUND),
                convert(UIContents.LOCKED, GenericUIFactory::applyCommonViewStyle)
        );
        DoubleUIField target = new DoubleUIField(
                d -> boundBlockEntity(boundPos, AbstractDynamicMotor.class).ifPresent(be -> be.getController().setTarget(d)),
                () -> boundBlockEntity(boundPos, AbstractDynamicMotor.class).map(be -> be.getController().getTarget()).orElse(0.0),
                convert(UIContents.TARGET, GenericUIFactory::applyCommonTitleStyle)
        );
        OptionUIField<TargetMode> toggle_mode = new OptionUIField<>(
                m -> boundBlockEntity(boundPos, AbstractDynamicMotor.class).ifPresent(be -> be.setTargetMode(m)),
                () -> boundBlockEntity(boundPos, AbstractDynamicMotor.class).map(AbstractDynamicMotor::getTargetMode).orElse(TargetMode.POSITION),
                TargetMode.class,
                convert(UIContents.MODE, GenericUIFactory::applyCommonTitleStyle)
        );
        OptionUIField<CheatMode> toggle_cheat = new OptionUIField<>(
                m -> boundBlockEntity(boundPos, AbstractDynamicMotor.class).ifPresent(be -> be.setCheatMode(m)),
                () -> boundBlockEntity(boundPos, AbstractDynamicMotor.class).map(AbstractDynamicMotor::getCheatMode).orElse(CheatMode.NONE),
                CheatMode.class,
                convert(UIContents.CHEAT, GenericUIFactory::applyCommonTitleStyle)
        );
        OptionUIField<LockMode> toggle_lock_mode = new OptionUIField<>(
                m -> boundBlockEntity(boundPos, AbstractDynamicMotor.class).ifPresent(be -> be.setLockMode(m)),
                () -> boundBlockEntity(boundPos, AbstractDynamicMotor.class).map(AbstractDynamicMotor::getLockMode).orElse(LockMode.OFF),
                LockMode.class,
                convert(UIContents.AUTO_LOCK, GenericUIFactory::applyCommonTitleStyle)
        );
        Vector3dUIField offset = new Vector3dUIField(
                v -> boundBlockEntity(boundPos, AbstractDynamicMotor.class).ifPresent(be -> be.setOffset(v)),
                () -> boundBlockEntity(boundPos, AbstractDynamicMotor.class).map(AbstractMotor::getOffset).orElse(new Vector3d(0, 0, 0)),
                convert(UIContents.OFFSET, GenericUIFactory::applyCommonTitleStyle),
                25
        );
        Runnable alignLabels = () -> {
            alignLabel(current_view, lock_view, target);
            alignLabel(toggle_mode, toggle_cheat, toggle_lock_mode);
            alignLabel(toggle_mode.valueLabel(), toggle_cheat.valueLabel(), toggle_lock_mode.valueLabel());
        };
        return new GenericSettingScreen.builder(boundPos)
                .withRenderedStack(stack)
                .withTab(
                        "general",
                        new VerticalFlow.builder(boundPos)
                                .withPort(SharedKeys.VALUE, current_view)
                                .withPort(SharedKeys.IS_LOCKED, lock_view)
                                .withPort(SharedKeys.TARGET, target)
                                .withPort(AbstractDynamicMotor.OFFSET, offset)
                                .withPort(SharedKeys.TARGET_MODE, toggle_mode)
                                .withPort(SharedKeys.CHEAT_MODE, toggle_cheat)
                                .withPort(SharedKeys.LOCK_MODE, toggle_lock_mode)
                                .withPreDoLayout(alignLabels)
                                .build()
                )
                .withTab(
                        "redstone",
                        createTerminalDeviceTab(boundPos)
                )
                .withTab(
                        "controller",
                        createControllerTab(boundPos)
                )
                .withTickTask(createSyncTasks(boundPos, SharedKeys.IS_LOCKED, SharedKeys.VALUE))
                .build();

    }

    public static GenericSettingScreen createDynamicSliderScreen(BlockPos boundPos, ItemStack stack){
        DoubleUIView current_view = DoubleUIView.of(
                () -> boundBlockEntity(boundPos, DynamicSliderBlockEntity.class).map(be -> String.format("%.4f", be.getController().getValue())).orElse("Not Found"),
                convert(UIContents.CURRENT, GenericUIFactory::applyCommonViewStyle)
        );
        ComponentUIView lock_view = new ComponentUIView(
                () -> boundBlockEntity(boundPos, DynamicSliderBlockEntity.class).map(be -> lockViewComponent(be.isLocked())).orElse(NOT_FOUND),
                convert(UIContents.LOCKED, GenericUIFactory::applyCommonViewStyle)
        );
        DoubleUIField target = new DoubleUIField(
                d -> boundBlockEntity(boundPos, DynamicSliderBlockEntity.class).ifPresent(be -> be.getController().setTarget(d)),
                () -> boundBlockEntity(boundPos, DynamicSliderBlockEntity.class).map(be -> be.getController().getTarget()).orElse(0.0),
                convert(UIContents.TARGET, GenericUIFactory::applyCommonTitleStyle)
        );
        OptionUIField<TargetMode> toggle_mode = new OptionUIField<>(
                m -> boundBlockEntity(boundPos, DynamicSliderBlockEntity.class).ifPresent(be -> be.setTargetMode(m)),
                () -> boundBlockEntity(boundPos, DynamicSliderBlockEntity.class).map(DynamicSliderBlockEntity::getTargetMode).orElse(TargetMode.POSITION),
                TargetMode.class,
                convert(UIContents.MODE, GenericUIFactory::applyCommonTitleStyle)
        );
        OptionUIField<CheatMode> toggle_cheat = new OptionUIField<>(
                m -> boundBlockEntity(boundPos, DynamicSliderBlockEntity.class).ifPresent(be -> be.setCheatMode(m)),
                () -> boundBlockEntity(boundPos, DynamicSliderBlockEntity.class).map(DynamicSliderBlockEntity::getCheatMode).orElse(CheatMode.NONE),
                CheatMode.class,
                convert(UIContents.CHEAT, GenericUIFactory::applyCommonTitleStyle)
        );
        OptionUIField<LockMode> toggle_lock_mode = new OptionUIField<>(
                m -> boundBlockEntity(boundPos, DynamicSliderBlockEntity.class).ifPresent(be -> be.setLockMode(m)),
                () -> boundBlockEntity(boundPos, DynamicSliderBlockEntity.class).map(DynamicSliderBlockEntity::getLockMode).orElse(LockMode.OFF),
                LockMode.class,
                convert(UIContents.AUTO_LOCK, GenericUIFactory::applyCommonTitleStyle)
        );
        Runnable alignLabels = () -> {
            alignLabel(current_view, lock_view, target);
            alignLabel(toggle_mode, toggle_cheat, toggle_lock_mode);
            alignLabel(toggle_mode.valueLabel(), toggle_cheat.valueLabel(), toggle_lock_mode.valueLabel());
        };
        return new GenericSettingScreen.builder(boundPos)
                .withRenderedStack(stack)
                .withTab(
                        "general",
                        new VerticalFlow.builder(boundPos)
                                .withPort(SharedKeys.VALUE, current_view)
                                .withPort(SharedKeys.IS_LOCKED, lock_view)
                                .withPort(SharedKeys.TARGET, target)
                                .withPort(SharedKeys.TARGET_MODE, toggle_mode)
                                .withPort(SharedKeys.CHEAT_MODE, toggle_cheat)
                                .withPort(SharedKeys.LOCK_MODE, toggle_lock_mode)
                                .withPreDoLayout(alignLabels)
                                .build()
                )
                .withTab(
                        "redstone",
                        createTerminalDeviceTab(boundPos)
                )
                .withTab(
                        "controller",
                        createControllerTab(boundPos)
                )
                .withTickTask(createSyncTasks(boundPos, SharedKeys.IS_LOCKED, SharedKeys.VALUE))
                .build();

    }

    public static GenericSettingScreen createPeripheralInterfaceScreen(BlockPos boundPos){
        ComponentUIView type_view = new ComponentUIView(
                () -> boundBlockEntity(boundPos, ReceiverBlockEntity.class).map(be -> Component.literal(be.getClientViewType())).orElse(NOT_FOUND.copy()),
                convert(UIContents.TYPE, GenericUIFactory::applyCommonViewStyle)
        );

        PeripheralKeyUIField key_field = new PeripheralKeyUIField(
                k -> boundBlockEntity(boundPos, ReceiverBlockEntity.class).ifPresent(be -> be.dispatchKey(k)),
                () -> boundBlockEntity(boundPos, ReceiverBlockEntity.class).map(ReceiverBlockEntity::getClientViewKey).orElse(PeripheralKey.NULL)
        );

        key_field.getNameLabel().withTextStyle(GenericUIFactory::applyCommonTitleStyle);
        key_field.getProtocolLabel().withTextStyle(GenericUIFactory::applyCommonTitleStyle);

        return new GenericSettingScreen.builder(boundPos)
                .withRenderedStack(ControlCraftBlocks.RECEIVER_BLOCK.asStack())
                .withTab(
                        "general",
                        new VerticalFlow.builder(boundPos)
                                .withPort(ReceiverBlockEntity.PERIPHERAL_TYPE, type_view)
                                .withPort(ReceiverBlockEntity.PERIPHERAL, key_field)
                                .build()
                )
                .withBackground(ControlCraftGuiTextures.SIMPLE_BACKGROUND_HALF)
                .build();
    }

    public static GenericSettingScreen createSpatialAnchorScreen(BlockPos pos){
        DoubleUIField offset_field = new DoubleUIField(
                d -> boundBlockEntity(pos, SpatialAnchorBlockEntity.class).ifPresent(be -> be.setAnchorOffset(d)),
                () -> boundBlockEntity(pos, SpatialAnchorBlockEntity.class).map(SpatialAnchorBlockEntity::getAnchorOffset).orElse(0.0),
                UIContents.SPATIAL_OFFSET
        );
        BasicUIField<Long> protocol_field = new BasicUIField<>(
                l -> boundBlockEntity(pos, SpatialAnchorBlockEntity.class).ifPresent(be -> be.setProtocol(ParseUtils.tryParseLong(l))),
                () -> boundBlockEntity(pos, SpatialAnchorBlockEntity.class).map(SpatialAnchorBlockEntity::getProtocol).orElse(0L) + "",
                UIContents.PROTOCOL,
                l -> l + "",
                ParseUtils::tryParseLong
        );

        BooleanUIField is_running_field = new BooleanUIField(
                b -> boundBlockEntity(pos, SpatialAnchorBlockEntity.class).ifPresent(be -> be.setRunning(b)),
                () -> boundBlockEntity(pos, SpatialAnchorBlockEntity.class).map(SpatialAnchorBlockEntity::isRunning).orElse(false),
                ExposedFieldType.IS_RUNNING
        );

        BooleanUIField is_static_field = new BooleanUIField(
                b -> boundBlockEntity(pos, SpatialAnchorBlockEntity.class).ifPresent(be -> be.setStatic(b)),
                () -> boundBlockEntity(pos, SpatialAnchorBlockEntity.class).map(SpatialAnchorBlockEntity::isStatic).orElse(false),
                ExposedFieldType.IS_STATIC
        );

        Runnable alignLabels = () -> {
            alignLabel(offset_field, protocol_field);
            alignLabel(is_running_field, is_static_field);
        };
        return new GenericSettingScreen.builder(pos)
                .withRenderedStack(ControlCraftBlocks.SPATIAL_ANCHOR_BLOCK.asStack())
                .withTab(
                        "general",
                        new VerticalFlow.builder(pos)
                                .withPort(SpatialAnchorBlockEntity.OFFSET, offset_field)
                                .withPort(SpatialAnchorBlockEntity.PROTOCOL, protocol_field)
                                .withPort(SpatialAnchorBlockEntity.IS_RUNNING, is_running_field)
                                .withPort(SpatialAnchorBlockEntity.IS_STATIC, is_static_field)
                                .withPreDoLayout(alignLabels)
                                .build()
                )
                .withTab(
                        "schedule",
                        createScheduleTab(pos)
                )
                .withTab(
                        "device",
                        createTerminalDeviceTab(pos)
                )
                .build();
    }

    public static GenericSettingScreen createKinematicDeviceScreen(BlockPos boundPos, ItemStack stack){

        DoubleUIView current_view = DoubleUIView.of(
                () -> boundBlockEntity(boundPos, IKinematicUIDevice.class).map(be -> String.format("%.4f", be.getController().getTarget())).orElse("Not Found"),
                convert(UIContents.CURRENT, GenericUIFactory::applyCommonViewStyle)
        );

        DoubleUIField target_field = new DoubleUIField(
                d -> boundBlockEntity(boundPos, IKinematicUIDevice.class).ifPresent(be -> be.getController().setControlTarget(d)),
                () -> boundBlockEntity(boundPos, IKinematicUIDevice.class).map(be -> be.getController().getControlTarget()).orElse(0.0),
                convert(UIContents.TARGET, GenericUIFactory::applyCommonTitleStyle)
        );

        DoubleUIField compliance_field = new DoubleUIField(
                d -> boundBlockEntity(boundPos, IKinematicUIDevice.class).ifPresent(be -> be.setCompliance(Math.pow(10, d))),
                () -> boundBlockEntity(boundPos, IKinematicUIDevice.class).map(be -> Math.log10(be.getCompliance())).orElse(0.0),
                convert(UIContents.COMPLIANCE, GenericUIFactory::applyCommonTitleStyle)
        );

        Vector3dUIField offset = new Vector3dUIField(
                v -> boundBlockEntity(boundPos, IKinematicUIDevice.class).ifPresent(be -> be.setOffset(v)),
                () -> boundBlockEntity(boundPos, IKinematicUIDevice.class).map(IKinematicUIDevice::getOffset).orElse(new Vector3d(0, 0, 0)),
                convert(UIContents.OFFSET, GenericUIFactory::applyCommonTitleStyle),
                25
        );

        OptionUIField<TargetMode> toggle_mode = new OptionUIField<>(
                m -> boundBlockEntity(boundPos, IKinematicUIDevice.class).ifPresent(be -> be.setTargetMode(m)),
                () -> boundBlockEntity(boundPos, IKinematicUIDevice.class).map(IKinematicUIDevice::getTargetMode).orElse(TargetMode.POSITION),
                TargetMode.class,
                convert(UIContents.MODE, GenericUIFactory::applyCommonTitleStyle)
        );

        Runnable alignLabels = () -> alignLabel(current_view, target_field, compliance_field, toggle_mode);

        return new GenericSettingScreen.builder(boundPos)
                .withTab(
                        "general",
                        new VerticalFlow.builder(boundPos)
                                .withPort(SharedKeys.VALUE, current_view)
                                .withPort(SharedKeys.TARGET, target_field)
                                .withPort(SharedKeys.COMPLIANCE, compliance_field)
                                .withPort(AbstractMotor.OFFSET, offset)
                                .withPort(SharedKeys.TARGET_MODE, toggle_mode)
                                .withPreDoLayout(alignLabels)
                                .build()
                )
                .withTab(
                        "device",
                        createTerminalDeviceTab(boundPos)
                )
                .withRenderedStack(stack)
                .withTickTask(createSyncTasks(boundPos, SharedKeys.VALUE))
                .build();
    }

    public static Runnable createSyncTasks(BlockPos boundPos, NetworkKey... keys){
        return () -> boundBlockEntity(boundPos, OptionalSyncedBlockEntity.class).ifPresent(
                be -> be.request(keys)
        );
    }


    public static void alignLabel(TitleLabelProvider... labels){
        int max_len = MathUtils.max(Arrays.stream(labels).map(l -> l.title().getWidth()).toArray(Integer[]::new));
        for (var label : labels){
            label.title().setWidth(max_len);
        }
    }

    public static void alignLabel(List<Label> labels){
        int max_len = MathUtils.max(labels.stream().map(AbstractWidget::getWidth).toArray(Integer[]::new));
        for (var label : labels){
            label.setWidth(max_len);
        }

    }

    public static void alignLabel(Label... labels){
        int max_len = MathUtils.max(Arrays.stream(labels).map(AbstractWidget::getWidth).toArray(Integer[]::new));
        for (var label : labels){
            label.setWidth(max_len);
        }

    }

    public static VerticalFlow createTerminalDeviceTab(BlockPos boundPos){
        return new VerticalFlow.builder(boundPos)
                .withPort(
                        ITerminalDevice.FIELD,
                        new TerminalDeviceUIField(() -> boundBlockEntity(boundPos, ITerminalDevice.class).orElse(null))
                ).build();
    }

    public static VerticalFlow createControllerTab(BlockPos boundPos){
        return new VerticalFlow.builder(boundPos)
                .withPort(
                        SharedKeys.CONTROLLER,
                        new DynamicControllerUIField(() -> boundBlockEntity(boundPos, IControllerProvider.class).orElse(null))
                ).build();
    }


    public static VerticalFlow createScheduleTab(BlockPos boundPos){
        return new VerticalFlow.builder(boundPos)
                .withPort(
                        SCHEDULE,
                        new SpatialScheduleUIField(() -> boundBlockEntity(boundPos, IScheduleProvider.class).orElse(null))
                ).build();
    }

    public static <T> Optional<T> boundBlockEntity(BlockPos p, Class<T> clazz){
        Minecraft mc = Minecraft.getInstance();
        return BlockEntityGetter.getLevelBlockEntityAt(mc.level, p, clazz);
    }

    public static LabelProvider convert(LabelProvider prov, UnaryOperator<Style> title){
        FormattedLabel original = prov.toDescriptiveLabel();
        return new LabelProvider(){
            @Override
            public FormattedLabel toDescriptiveLabel() {
                return original.withTextStyle(title);
            }

            @Override
            public FormattedLabel toUILabel() {
                return toDescriptiveLabel();
            }
        };

    }

}
