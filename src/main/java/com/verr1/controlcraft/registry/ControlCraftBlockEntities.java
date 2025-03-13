package com.verr1.controlcraft.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.verr1.controlcraft.content.blocks.anchor.AnchorBlock;
import com.verr1.controlcraft.content.blocks.anchor.AnchorBlockEntity;
import com.verr1.controlcraft.content.blocks.jet.JetBlock;
import com.verr1.controlcraft.content.blocks.jet.JetBlockEntity;
import com.verr1.controlcraft.content.blocks.jet.JetRudderBlock;
import com.verr1.controlcraft.content.blocks.jet.JetRudderBlockEntity;
import com.verr1.controlcraft.content.blocks.joints.*;
import com.verr1.controlcraft.content.blocks.motor.JointMotorBlock;
import com.verr1.controlcraft.content.blocks.motor.JointMotorBlockEntity;
import com.verr1.controlcraft.content.blocks.motor.RevoluteMotorBlock;
import com.verr1.controlcraft.content.blocks.motor.RevoluteMotorBlockEntity;
import com.verr1.controlcraft.content.blocks.propeller.PropellerBlock;
import com.verr1.controlcraft.content.blocks.propeller.PropellerBlockEntity;
import com.verr1.controlcraft.content.blocks.propeller.PropellerControllerBlock;
import com.verr1.controlcraft.content.blocks.propeller.PropellerControllerBlockEntity;
import com.verr1.controlcraft.content.blocks.receiver.ReceiverBlock;
import com.verr1.controlcraft.content.blocks.receiver.ReceiverBlockEntity;
import com.verr1.controlcraft.content.blocks.slider.SliderBlock;
import com.verr1.controlcraft.content.blocks.slider.SliderBlockEntity;
import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlock;
import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlockEntity;
import com.verr1.controlcraft.content.blocks.terminal.TerminalBlock;
import com.verr1.controlcraft.content.blocks.terminal.TerminalBlockEntity;
import com.verr1.controlcraft.content.blocks.transmitter.TransmitterBlock;
import com.verr1.controlcraft.content.blocks.transmitter.TransmitterBlockEntity;
import com.verr1.controlcraft.render.*;

import static com.verr1.controlcraft.ControlCraft.REGISTRATE;

public class ControlCraftBlockEntities {

    public static final BlockEntityEntry<AnchorBlockEntity> ANCHOR_BLOCKENTITY = REGISTRATE
            .blockEntity(AnchorBlock.ID, AnchorBlockEntity::new)
            .validBlock(ControlCraftBlocks.ANCHOR_BLOCK)
            .register();

    public static final BlockEntityEntry<RevoluteMotorBlockEntity> SERVO_MOTOR_BLOCKENTITY = REGISTRATE
            .blockEntity(RevoluteMotorBlock.ID, RevoluteMotorBlockEntity::new)
            .validBlock(ControlCraftBlocks.SERVO_MOTOR_BLOCK)
            .renderer(()-> RevoluteMotorRenderer::new)
            .register();

    public static final BlockEntityEntry<JointMotorBlockEntity> JOINT_MOTOR_BLOCKENTITY = REGISTRATE
            .blockEntity(JointMotorBlock.ID, JointMotorBlockEntity::new)
            .validBlock(ControlCraftBlocks.JOINT_MOTOR_BLOCK)
            .register();

    public static final BlockEntityEntry<SliderBlockEntity> SLIDER_CONTROLLER_BLOCKENTITY = REGISTRATE
            .blockEntity(SliderBlock.ID, SliderBlockEntity::new)
            .validBlock(ControlCraftBlocks.SLIDER_CONTROLLER_BLOCK)
            .renderer(() -> SliderRenderer::new)
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

    public static final BlockEntityEntry<TransmitterBlockEntity> TRANSMITTER_BLOCKENTITY = REGISTRATE
            .blockEntity(TransmitterBlock.ID, TransmitterBlockEntity::new)
            .validBlock(ControlCraftBlocks.TRANSMITTER_BLOCK)
            .register();

    public static final BlockEntityEntry<ReceiverBlockEntity> RECEIVER_BLOCKENTITY = REGISTRATE
            .blockEntity(ReceiverBlock.ID, ReceiverBlockEntity::new)
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

    public static void register(){

    }
}
