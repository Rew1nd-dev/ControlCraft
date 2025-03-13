package com.verr1.controlcraft.registry;

import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.content.blocks.anchor.AnchorBlock;
import com.verr1.controlcraft.content.blocks.jet.JetBlock;
import com.verr1.controlcraft.content.blocks.jet.JetRudderBlock;
import com.verr1.controlcraft.content.blocks.joints.FreeJointBlock;
import com.verr1.controlcraft.content.blocks.joints.PivotJointBlock;
import com.verr1.controlcraft.content.blocks.joints.RevoluteJointBlock;
import com.verr1.controlcraft.content.blocks.motor.JointMotorBlock;
import com.verr1.controlcraft.content.blocks.motor.RevoluteMotorBlock;
import com.verr1.controlcraft.content.blocks.propeller.PropellerBlock;
import com.verr1.controlcraft.content.blocks.propeller.PropellerControllerBlock;
import com.verr1.controlcraft.content.blocks.receiver.ReceiverBlock;
import com.verr1.controlcraft.content.blocks.slider.SliderBlock;
import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlock;
import com.verr1.controlcraft.content.blocks.spatial.SpatialMovementBehavior;
import com.verr1.controlcraft.content.blocks.terminal.TerminalBlock;
import com.verr1.controlcraft.content.blocks.transmitter.TransmitterBlock;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.AllMovementBehaviours.movementBehaviour;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.verr1.controlcraft.ControlCraft.REGISTRATE;

public class ControlCraftBlocks {
    static {
        REGISTRATE.setCreativeTab(ControlCraftCreativeTabs.TAB);
    }

    public static final BlockEntry<AnchorBlock> ANCHOR_BLOCK = REGISTRATE
            .block(AnchorBlock.ID, AnchorBlock::new)
            .initialProperties(SharedProperties::stone)
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .item()
            .properties(p -> p.rarity(Rarity.EPIC))
            .transform(customItemModel())
            .lang("Gravitational Anchor")
            .register();

    public static final BlockEntry<RevoluteMotorBlock> SERVO_MOTOR_BLOCK = REGISTRATE
            .block(RevoluteMotorBlock.ID, RevoluteMotorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .properties(p -> p.rarity(Rarity.RARE))
            .transform(customItemModel())
            .lang("Servo Motor")
            .register();

    public static final BlockEntry<JointMotorBlock> JOINT_MOTOR_BLOCK = REGISTRATE
            .block(JointMotorBlock.ID, JointMotorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalAxisBlockProvider()
            )
            .item()
            .properties(p -> p.rarity(Rarity.RARE))
            .transform(customItemModel())
            .lang("Joint Motor")
            .register();

    public static final BlockEntry<SliderBlock> SLIDER_CONTROLLER_BLOCK = REGISTRATE
            .block(SliderBlock.ID, SliderBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .properties(p -> p.rarity(Rarity.EPIC))
            .transform(customItemModel())
            .lang("Physical Piston")
            .register();

    public static final BlockEntry<RevoluteJointBlock> REVOLUTE_JOINT_BLOCK = REGISTRATE
            .block(RevoluteJointBlock.ID, RevoluteJointBlock::new)
            .initialProperties(SharedProperties::stone)
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
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .item()
            .transform(customItemModel())
            .lang("Wireless Redstone Terminal")
            .register();

    public static final BlockEntry<TransmitterBlock> TRANSMITTER_BLOCK = REGISTRATE
            .block(TransmitterBlock.ID, TransmitterBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .lang("Peripheral Proxy")
            .register();


    public static final BlockEntry<ReceiverBlock> RECEIVER_BLOCK = REGISTRATE
            .block(ReceiverBlock.ID, ReceiverBlock::new)
            .initialProperties(SharedProperties::stone)
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
            .transform(TagGen.axeOrPickaxe())
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
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static void register(){
    }
}
