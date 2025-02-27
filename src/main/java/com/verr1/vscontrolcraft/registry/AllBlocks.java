package com.verr1.vscontrolcraft.registry;

import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.verr1.vscontrolcraft.blocks.anchor.AnchorBlock;
import com.verr1.vscontrolcraft.blocks.annihilator.AnnihilatorBlock;
import com.verr1.vscontrolcraft.blocks.camera.CameraBlock;
import com.verr1.vscontrolcraft.blocks.chunkLoader.ChunkLoaderBlock;
import com.verr1.vscontrolcraft.blocks.jet.JetBlock;
import com.verr1.vscontrolcraft.blocks.jetRudder.JetRudderBlock;
import com.verr1.vscontrolcraft.blocks.jointMotor.JointMotorBlock;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetBlock;
import com.verr1.vscontrolcraft.blocks.pivotJoint.PivotJointBlock;
import com.verr1.vscontrolcraft.blocks.revoluteJoint.RevoluteJointBlock;
import com.verr1.vscontrolcraft.blocks.revoluteJoint.RevoluteJointDataGenerator;
import com.verr1.vscontrolcraft.blocks.servoMotor.ServoMotorBlock;
import com.verr1.vscontrolcraft.blocks.slider.SliderControllerBlock;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.DirectionalAxialFlippableDataGenerator;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialAnchorBlock;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialAnchorDataGenerator;
import com.verr1.vscontrolcraft.blocks.spatialAnchor.SpatialMovementBehavior;
import com.verr1.vscontrolcraft.blocks.sphericalHinge.SphericalHingeBlock;
import com.verr1.vscontrolcraft.blocks.sphericalHinge.DirectionalAdjustableHingeDataGenerator;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerBlock;
import com.verr1.vscontrolcraft.blocks.terminal.TerminalBlock;
import com.verr1.vscontrolcraft.blocks.wingController.WingControllerBlock;
import com.verr1.vscontrolcraft.blocks.recevier.ReceiverBlock;
import com.verr1.vscontrolcraft.blocks.transmitter.TransmitterBlock;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerBlock;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerBlock;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.MapColor;


import static com.simibubi.create.AllMovementBehaviours.movementBehaviour;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.verr1.vscontrolcraft.ControlCraft.REGISTRATE;

public class AllBlocks {
    static {
        REGISTRATE.setCreativeTab(AllCreativeTabs.TAB);
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

    public static final BlockEntry<WingControllerBlock> WING_CONTROLLER_BLOCK = REGISTRATE
            .block(WingControllerBlock.ID, WingControllerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .lang("Flap Bearing")
            .register();

    public static final BlockEntry<SpinalyzerBlock> SPINALYZER_BLOCK = REGISTRATE
            .block(SpinalyzerBlock.ID, SpinalyzerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<CameraBlock> CAMERA_BLOCK = REGISTRATE
            .block(CameraBlock.ID, CameraBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<ServoMotorBlock> SERVO_MOTOR_BLOCK = REGISTRATE
            .block(ServoMotorBlock.ID, ServoMotorBlock::new)
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

    public static final BlockEntry<MagnetBlock> MAGNET_BLOCK = REGISTRATE
            .block(MagnetBlock.ID, MagnetBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .item()
            .transform(customItemModel())
            .lang("Magnet")
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

    public static final BlockEntry<SphericalHingeBlock> SPHERE_HINGE_BLOCK = REGISTRATE
            .block(SphericalHingeBlock.ID, SphericalHingeBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    DirectionalAdjustableHingeDataGenerator.generate()
            )
            .item()
            .transform(customItemModel())
            .lang("Spherical Hinge")
            .register();

    public static final BlockEntry<RevoluteJointBlock> REVOLUTE_JOINT_BLOCK = REGISTRATE
            .block(RevoluteJointBlock.ID, RevoluteJointBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    RevoluteJointDataGenerator.generate()
            )
            .item()
            .transform(customItemModel())
            .lang("Revolute Hinge")
            .register();

    public static final BlockEntry<PivotJointBlock> PIVOT_JOINT_BLOCK = REGISTRATE
            .block(PivotJointBlock.ID, PivotJointBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .blockstate(
                    DirectionalAdjustableHingeDataGenerator.generate()
            )
            .item()
            .transform(customItemModel())
            .lang("Pivot Hinge")
            .register();

    public static final BlockEntry<SliderControllerBlock> SLIDER_CONTROLLER_BLOCK = REGISTRATE
            .block(SliderControllerBlock.ID, SliderControllerBlock::new)
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

    public static final BlockEntry<AnnihilatorBlock> ANNIHILATOR_BLOCK = REGISTRATE
            .block(AnnihilatorBlock.ID, AnnihilatorBlock::new)
            .initialProperties(SharedProperties::stone)
            .blockstate(
                    BlockStateGen.directionalBlockProvider(true)
            )
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .item()
            .transform(customItemModel())
            .lang("Forget-Me-Not")
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

    public static final BlockEntry<SpatialAnchorBlock> SPATIAL_ANCHOR_BLOCK = REGISTRATE
            .block(SpatialAnchorBlock.ID, SpatialAnchorBlock::new)
            .initialProperties(SharedProperties::stone)
            .blockstate(
                    SpatialAnchorDataGenerator.generate()
            )
            .onRegister(movementBehaviour(new SpatialMovementBehavior()))
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .item()
            .properties(p -> p.rarity(Rarity.EPIC))
            .transform(customItemModel())
            .lang("Spatial Anchor")
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

    public static void register(){

    }
}
