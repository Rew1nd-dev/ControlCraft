package com.verr1.controlcraft.registry;

import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.verr1.controlcraft.content.blocks.anchor.AnchorBlock;
import com.verr1.controlcraft.content.blocks.anchor.AnchorBlockEntity;
import com.verr1.controlcraft.content.blocks.camera.CameraBlock;
import com.verr1.controlcraft.content.blocks.camera.CameraBlockEntity;
import com.verr1.controlcraft.content.blocks.flap.FlapBearingBlock;
import com.verr1.controlcraft.content.blocks.flap.FlapBearingBlockEntity;
import com.verr1.controlcraft.content.blocks.jet.JetBlock;
import com.verr1.controlcraft.content.blocks.jet.JetBlockEntity;
import com.verr1.controlcraft.content.blocks.jet.JetRudderBlock;
import com.verr1.controlcraft.content.blocks.jet.JetRudderBlockEntity;
import com.verr1.controlcraft.content.blocks.joints.*;
import com.verr1.controlcraft.content.blocks.kinetic.proxy.KineticProxyBlock;
import com.verr1.controlcraft.content.blocks.kinetic.proxy.KineticProxyBlockEntity;
import com.verr1.controlcraft.content.blocks.kinetic.resistor.KineticResistorBlock;
import com.verr1.controlcraft.content.blocks.kinetic.resistor.KineticResistorBlockEntity;
import com.verr1.controlcraft.content.blocks.loader.ChunkLoaderBlock;
import com.verr1.controlcraft.content.blocks.loader.ChunkLoaderBlockEntity;
import com.verr1.controlcraft.content.blocks.motor.*;
import com.verr1.controlcraft.content.blocks.propeller.PropellerBlock;
import com.verr1.controlcraft.content.blocks.propeller.PropellerBlockEntity;
import com.verr1.controlcraft.content.blocks.propeller.PropellerControllerBlock;
import com.verr1.controlcraft.content.blocks.propeller.PropellerControllerBlockEntity;
import com.verr1.controlcraft.content.blocks.receiver.PeripheralInterfaceBlock;
import com.verr1.controlcraft.content.blocks.receiver.PeripheralInterfaceBlockEntity;
import com.verr1.controlcraft.content.blocks.slider.DynamicSliderBlock;
import com.verr1.controlcraft.content.blocks.slider.DynamicSliderBlockEntity;
import com.verr1.controlcraft.content.blocks.slider.KinematicSliderBlock;
import com.verr1.controlcraft.content.blocks.slider.KinematicSliderBlockEntity;
import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlock;
import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlockEntity;
import com.verr1.controlcraft.content.blocks.spinalyzer.SpinalyzerBlock;
import com.verr1.controlcraft.content.blocks.spinalyzer.SpinalyzerBlockEntity;
import com.verr1.controlcraft.content.blocks.terminal.TerminalBlock;
import com.verr1.controlcraft.content.blocks.transmitter.PeripheralProxyBlock;
import com.verr1.controlcraft.content.blocks.transmitter.PeripheralProxyBlockEntity;
import com.verr1.controlcraft.content.blocks.terminal.TerminalBlockEntity;
import com.verr1.controlcraft.render.*;

import static com.verr1.controlcraft.ControlCraft.REGISTRATE;

public class ControlCraftBlockEntities {

    public static final BlockEntityEntry<ChunkLoaderBlockEntity> CHUNK_LOADER_BLOCKENTITY = REGISTRATE
            .blockEntity(ChunkLoaderBlock.ID, ChunkLoaderBlockEntity::new)
            .validBlock(ControlCraftBlocks.CHUNK_LOADER)
            .register();

    public static final BlockEntityEntry<AnchorBlockEntity> ANCHOR_BLOCKENTITY = REGISTRATE
            .blockEntity(AnchorBlock.ID, AnchorBlockEntity::new)
            .validBlock(ControlCraftBlocks.ANCHOR_BLOCK)
            .register();

    public static final BlockEntityEntry<DynamicRevoluteMotorBlockEntity> SERVO_MOTOR_BLOCKENTITY = REGISTRATE
            .blockEntity(DynamicRevoluteMotorBlock.ID, DynamicRevoluteMotorBlockEntity::new)
            .validBlock(ControlCraftBlocks.SERVO_MOTOR_BLOCK)
            .renderer(()-> DynamicRevoluteMotorRenderer::new)
            .register();

    public static final BlockEntityEntry<DynamicJointMotorBlockEntity> JOINT_MOTOR_BLOCKENTITY = REGISTRATE
            .blockEntity(DynamicJointMotorBlock.ID, DynamicJointMotorBlockEntity::new)
            .validBlock(ControlCraftBlocks.JOINT_MOTOR_BLOCK)
            .register();

    public static final BlockEntityEntry<DynamicSliderBlockEntity> SLIDER_CONTROLLER_BLOCKENTITY = REGISTRATE
            .blockEntity(DynamicSliderBlock.ID, DynamicSliderBlockEntity::new)
            .validBlock(ControlCraftBlocks.SLIDER_CONTROLLER_BLOCK)
            .renderer(() -> DynamicSliderRenderer::new)
            .register();

    public static final BlockEntityEntry<RevoluteJointBlockEntity> REVOLUTE_JOINT_BLOCKENTITY = REGISTRATE
            .blockEntity(RevoluteJointBlock.ID, RevoluteJointBlockEntity::new)
            .validBlock(ControlCraftBlocks.REVOLUTE_JOINT_BLOCK)
            .register();

    public static final BlockEntityEntry<FreeJointBlockEntity> SPHERE_HINGE_BLOCKENTITY = REGISTRATE
            .blockEntity(FreeJointBlock.ID, FreeJointBlockEntity::new)
            .validBlock(ControlCraftBlocks.SPHERE_HINGE_BLOCK)
            .register();

    public static final BlockEntityEntry<PivotJointBlockEntity> PIVOT_JOINT_BLOCKENTITY = REGISTRATE
            .blockEntity(PivotJointBlock.ID, PivotJointBlockEntity::new)
            .validBlock(ControlCraftBlocks.PIVOT_JOINT_BLOCK)
            .register();

    public static final BlockEntityEntry<TerminalBlockEntity> TERMINAL_BLOCKENTITY = REGISTRATE
            .blockEntity(TerminalBlock.ID, TerminalBlockEntity::new)
            .validBlock(ControlCraftBlocks.TERMINAL_BLOCK)
            .register();

    public static final BlockEntityEntry<PeripheralProxyBlockEntity> TRANSMITTER_BLOCKENTITY = REGISTRATE
            .blockEntity(PeripheralProxyBlock.ID, PeripheralProxyBlockEntity::new)
            .validBlock(ControlCraftBlocks.TRANSMITTER_BLOCK)
            .register();

    public static final BlockEntityEntry<PeripheralInterfaceBlockEntity> RECEIVER_BLOCKENTITY = REGISTRATE
            .blockEntity(PeripheralInterfaceBlock.ID, PeripheralInterfaceBlockEntity::new)
            .validBlock(ControlCraftBlocks.RECEIVER_BLOCK)
            .register();

    public static final BlockEntityEntry<SpatialAnchorBlockEntity> SPATIAL_ANCHOR_BLOCKENTITY = REGISTRATE
            .blockEntity(SpatialAnchorBlock.ID, SpatialAnchorBlockEntity::new)
            .validBlock(ControlCraftBlocks.SPATIAL_ANCHOR_BLOCK)
            .renderer(() -> SpatialAnchorRenderer::new)
            .register();

    public static final BlockEntityEntry<JetBlockEntity> JET_BLOCKENTITY = REGISTRATE
            .blockEntity(JetBlock.ID, JetBlockEntity::new)
            .validBlock(ControlCraftBlocks.JET_BLOCK)
            .register();

    public static final BlockEntityEntry<JetRudderBlockEntity> JET_RUDDER_BLOCKENTITY = REGISTRATE
            .blockEntity(JetRudderBlock.ID, JetRudderBlockEntity::new)
            .validBlock(ControlCraftBlocks.JET_RUDDER_BLOCK)
            .renderer(() -> JetRudderRenderer::new)
            .register();

    public static final BlockEntityEntry<PropellerControllerBlockEntity> PROPELLER_CONTROLLER_BLOCKENTITY = REGISTRATE
            .blockEntity(PropellerControllerBlock.ID, PropellerControllerBlockEntity::new)
            .validBlock(ControlCraftBlocks.PROPELLER_CONTROLLER)
            .register();

    public static final BlockEntityEntry<PropellerBlockEntity> PROPELLER_BLOCKENTITY = REGISTRATE
            .blockEntity(PropellerBlock.ID, PropellerBlockEntity::new)
            .validBlock(ControlCraftBlocks.PROPELLER_BLOCK)
            .renderer(() -> PropellerRenderer::new)
            .register();

    public static final BlockEntityEntry<SpinalyzerBlockEntity> SPINALYZER_BLOCKENTITY = REGISTRATE
            .blockEntity(SpinalyzerBlock.ID, SpinalyzerBlockEntity::new)
            .validBlock(ControlCraftBlocks.SPINALYZER_BLOCK)
            .renderer(() -> SpinalyzerRenderer::new)
            .register();

    public static final BlockEntityEntry<FlapBearingBlockEntity> WING_CONTROLLER_BLOCKENTITY = REGISTRATE
            .blockEntity(FlapBearingBlock.ID, FlapBearingBlockEntity::new)
            .validBlock(ControlCraftBlocks.WING_CONTROLLER_BLOCK)
            .renderer(() -> FlapBearingRenderer::new)
            .register();

    public static final BlockEntityEntry<KinematicRevoluteMotorBlockEntity> CONSTRAINT_SERVO_MOTOR_BLOCK = REGISTRATE
            .blockEntity(KinematicRevoluteMotorBlock.ID, KinematicRevoluteMotorBlockEntity::new)
            .validBlock(ControlCraftBlocks.CONSTRAINT_SERVO_MOTOR_BLOCK)
            .renderer(() -> KinematicRevoluteMotorRenderer::new)
            .register();

    public static final BlockEntityEntry<KinematicJointMotorBlockEntity> CONSTRAINT_JOINT_MOTOR_BLOCKENTITY = REGISTRATE
            .blockEntity(KinematicJointMotorBlock.ID, KinematicJointMotorBlockEntity::new)
            .validBlock(ControlCraftBlocks.CONSTRAINT_JOINT_MOTOR_BLOCK)
            .register();

    public static final BlockEntityEntry<KinematicSliderBlockEntity> CONSTRAINT_SLIDER_BLOCKENTITY = REGISTRATE
            .blockEntity(KinematicSliderBlock.ID, KinematicSliderBlockEntity::new)
            .validBlock(ControlCraftBlocks.CONSTRAINT_SLIDER_BLOCK)
            .renderer(() -> KinematicSliderRenderer::new)
            .register();

    public static final BlockEntityEntry<CameraBlockEntity> CAMERA_BLOCKENTITY = REGISTRATE
            .blockEntity(CameraBlock.ID, CameraBlockEntity::new)
            .validBlock(ControlCraftBlocks.CAMERA_BLOCK)
            .renderer(()-> CameraRenderer::new)
            .register();

    public static final BlockEntityEntry<KineticProxyBlockEntity> KINETIC_PROXY_BLOCKENTITY = REGISTRATE
            .blockEntity(KineticProxyBlock.ID, KineticProxyBlockEntity::new)
            .instance(() -> ShaftInstance::new, false)
            .validBlock(ControlCraftBlocks.KINETIC_PROXY_BLOCK)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static final BlockEntityEntry<KineticResistorBlockEntity> KINETIC_RESISTOR_BLOCKENTITY = REGISTRATE
            .blockEntity(KineticResistorBlock.ID, KineticResistorBlockEntity::new)
            .instance(() -> ShaftInstance::new, false)
            .validBlock(ControlCraftBlocks.KINETIC_RESISTOR_BLOCK)
            .renderer(() -> ShaftRenderer::new)
            .register();

    public static void register(){

    }
}
