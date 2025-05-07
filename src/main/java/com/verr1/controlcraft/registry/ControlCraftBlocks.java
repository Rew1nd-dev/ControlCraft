package com.verr1.controlcraft.registry;

import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.verr1.controlcraft.content.blocks.anchor.AnchorBlock;
import com.verr1.controlcraft.content.blocks.camera.CameraBlock;
import com.verr1.controlcraft.content.blocks.flap.FlapBearingBlock;
import com.verr1.controlcraft.content.blocks.jet.JetBlock;
import com.verr1.controlcraft.content.blocks.jet.JetRudderBlock;
import com.verr1.controlcraft.content.blocks.joints.FreeJointBlock;
import com.verr1.controlcraft.content.blocks.joints.PivotJointBlock;
import com.verr1.controlcraft.content.blocks.joints.RevoluteJointBlock;
import com.verr1.controlcraft.content.blocks.kinetic.proxy.KineticProxyBlock;
import com.verr1.controlcraft.content.blocks.kinetic.resistor.KineticResistorBlock;
import com.verr1.controlcraft.content.blocks.loader.ChunkLoaderBlock;
import com.verr1.controlcraft.content.blocks.motor.KinematicJointMotorBlock;
import com.verr1.controlcraft.content.blocks.motor.KinematicRevoluteMotorBlock;
import com.verr1.controlcraft.content.blocks.motor.DynamicJointMotorBlock;
import com.verr1.controlcraft.content.blocks.motor.DynamicRevoluteMotorBlock;
import com.verr1.controlcraft.content.blocks.propeller.PropellerBlock;
import com.verr1.controlcraft.content.blocks.propeller.PropellerControllerBlock;
import com.verr1.controlcraft.content.blocks.receiver.PeripheralInterfaceBlock;
import com.verr1.controlcraft.content.blocks.slider.DynamicSliderBlock;
import com.verr1.controlcraft.content.blocks.slider.KinematicSliderBlock;
import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlock;
import com.verr1.controlcraft.content.blocks.spatial.SpatialMovementBehavior;
import com.verr1.controlcraft.content.blocks.spinalyzer.SpinalyzerBlock;
import com.verr1.controlcraft.content.blocks.terminal.TerminalBlock;
import com.verr1.controlcraft.content.blocks.transmitter.PeripheralProxyBlock;
import com.verr1.controlcraft.content.items.KinematicDeviceBlockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.AllMovementBehaviours.movementBehaviour;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.verr1.controlcraft.ControlCraft.REGISTRATE;

public class ControlCraftBlocks {
    static {
        REGISTRATE.setCreativeTab(ControlCraftCreativeTabs.TAB);
    }

    public static final BlockEntry<ChunkLoaderBlock> CHUNK_LOADER = REGISTRATE
            .block(ChunkLoaderBlock.ID, ChunkLoaderBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(TagGen.axeOrPickaxe())
            .blockstate(
                    BlockStateGen.horizontalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<AnchorBlock> ANCHOR_BLOCK = REGISTRATE
            .block(AnchorBlock.ID, AnchorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .item()
            .properties(p -> p.rarity(Rarity.EPIC))
            .transform(customItemModel())
            .lang("Gravitational Anchor")
            .register();

    public static final BlockEntry<DynamicRevoluteMotorBlock> SERVO_MOTOR_BLOCK = REGISTRATE
            .block(DynamicRevoluteMotorBlock.ID, DynamicRevoluteMotorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .properties(p -> p.rarity(Rarity.RARE))
            .transform(customItemModel())
            .lang("Dynamic Servo Motor")
            .register();

    public static final BlockEntry<DynamicJointMotorBlock> JOINT_MOTOR_BLOCK = REGISTRATE
            .block(DynamicJointMotorBlock.ID, DynamicJointMotorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalAxisBlockProvider()
            )
            .item()
            .properties(p -> p.rarity(Rarity.RARE))
            .transform(customItemModel())
            .lang("Dynamic Joint Motor")
            .register();

    public static final BlockEntry<DynamicSliderBlock> SLIDER_CONTROLLER_BLOCK = REGISTRATE
            .block(DynamicSliderBlock.ID, DynamicSliderBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .properties(p -> p.rarity(Rarity.RARE))
            .transform(customItemModel())
            .lang("Dynamic Physical Piston")
            .register();

    public static final BlockEntry<RevoluteJointBlock> REVOLUTE_JOINT_BLOCK = REGISTRATE
            .block(RevoluteJointBlock.ID, RevoluteJointBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    RevoluteJointBlock.RevoluteJointDataGenerator.generate()
            )
            .item()
            .transform(customItemModel())
            .lang("Revolute Hinge")
            .register();

    public static final BlockEntry<FreeJointBlock> SPHERE_HINGE_BLOCK = REGISTRATE
            .block(FreeJointBlock.ID, FreeJointBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    FreeJointBlock.DirectionalAdjustableHingeDataGenerator.generate()
            )
            .item()
            .transform(customItemModel())
            .lang("Spherical Hinge")
            .register();

    public static final BlockEntry<PivotJointBlock> PIVOT_JOINT_BLOCK = REGISTRATE
            .block(PivotJointBlock.ID, PivotJointBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    FreeJointBlock.DirectionalAdjustableHingeDataGenerator.generate()
            )
            .item()
            .transform(customItemModel())
            .lang("Pivot Hinge")
            .register();

    public static final BlockEntry<TerminalBlock> TERMINAL_BLOCK = REGISTRATE
            .block(TerminalBlock.ID, TerminalBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .item()
            .transform(customItemModel())
            .lang("Wireless Redstone Terminal")
            .register();

    public static final BlockEntry<PeripheralProxyBlock> TRANSMITTER_BLOCK = REGISTRATE
            .block(PeripheralProxyBlock.ID, PeripheralProxyBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .lang("Peripheral Proxy")
            .register();


    public static final BlockEntry<PeripheralInterfaceBlock> RECEIVER_BLOCK = REGISTRATE
            .block(PeripheralInterfaceBlock.ID, PeripheralInterfaceBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .lang("Peripheral Interface")
            .register();

    public static final BlockEntry<SpatialAnchorBlock> SPATIAL_ANCHOR_BLOCK = REGISTRATE
            .block(SpatialAnchorBlock.ID, SpatialAnchorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .blockstate(
                    SpatialAnchorBlock.SpatialAnchorDataGenerator.generate()
            )
            .onRegister(movementBehaviour(new SpatialMovementBehavior()))
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .item()
            .properties(p -> p.rarity(Rarity.EPIC))
            .transform(customItemModel())
            .lang("Spatial Anchor")
            .register();

    public static final BlockEntry<JetBlock> JET_BLOCK = REGISTRATE
            .block(JetBlock.ID, JetBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .item()
            .transform(customItemModel())
            .lang("Jet Engine")
            .register();

    public static final BlockEntry<JetRudderBlock> JET_RUDDER_BLOCK = REGISTRATE
            .block(JetRudderBlock.ID, JetRudderBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .item()
            .transform(customItemModel())
            .lang("Jet Rudder")
            .register();

    public static final BlockEntry<PropellerControllerBlock> PROPELLER_CONTROLLER = REGISTRATE
            .block(PropellerControllerBlock.ID, PropellerControllerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .transform(TagGen.axeOrPickaxe())
            .lang("Propeller Controller")
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<PropellerBlock> PROPELLER_BLOCK = REGISTRATE
            .block(PropellerBlock.ID, PropellerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<SpinalyzerBlock> SPINALYZER_BLOCK = REGISTRATE
            .block(SpinalyzerBlock.ID, SpinalyzerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<FlapBearingBlock> WING_CONTROLLER_BLOCK = REGISTRATE
            .block(FlapBearingBlock.ID, FlapBearingBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .lang("Flap Bearing")
            .register();

    public static final BlockEntry<KinematicRevoluteMotorBlock> CONSTRAINT_SERVO_MOTOR_BLOCK = REGISTRATE
            .block(KinematicRevoluteMotorBlock.ID, KinematicRevoluteMotorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item(KinematicDeviceBlockItem::new)
            .properties(p -> p.rarity(Rarity.create("legendary", style -> style
                    .applyFormat(ChatFormatting.RED)
                    .withBold(true))))
            .transform(customItemModel())
            .lang("Kinematic Servo Motor")
            .register();

    public static final BlockEntry<KinematicJointMotorBlock> CONSTRAINT_JOINT_MOTOR_BLOCK = REGISTRATE
            .block(KinematicJointMotorBlock.ID, KinematicJointMotorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .blockstate(
                    BlockStateGen.directionalAxisBlockProvider()
            )
            .item(KinematicDeviceBlockItem::new)
            .properties(p -> p.rarity(Rarity.create(
                    "legendary",
                    style -> style
                            .applyFormat(ChatFormatting.RED)
                            .withBold(true)
            )))
            .transform(customItemModel())
            .lang("Kinematic Joint Motor")
            .register();

    public static final BlockEntry<KinematicSliderBlock> CONSTRAINT_SLIDER_BLOCK = REGISTRATE
            .block(KinematicSliderBlock.ID, KinematicSliderBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item(KinematicDeviceBlockItem::new)
            .properties(p -> p.rarity(Rarity.create("legendary", style -> style
                    .applyFormat(ChatFormatting.RED)
                    .withBold(true))))
            .transform(customItemModel())
            .lang("Kinematic Physical Piston")
            .register();

    public static final BlockEntry<CameraBlock> CAMERA_BLOCK = REGISTRATE
            .block(CameraBlock.ID, CameraBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(64))
            .transform(TagGen.pickaxeOnly())
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .properties(p -> p.rarity(Rarity.RARE))
            .transform(customItemModel())
            .register();

    public static final BlockEntry<KineticProxyBlock> KINETIC_PROXY_BLOCK = REGISTRATE
            .block(KineticProxyBlock.ID, KineticProxyBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<KineticResistorBlock> KINETIC_RESISTOR_BLOCK = REGISTRATE
            .block(KineticResistorBlock.ID, KineticResistorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(32))
            .transform(TagGen.pickaxeOnly())
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();


    public static void register(){
    }
}
