package com.verr1.vscontrolcraft.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.verr1.vscontrolcraft.blocks.camera.CameraBlock;
import com.verr1.vscontrolcraft.blocks.camera.CameraBlockEntity;
import com.verr1.vscontrolcraft.blocks.camera.CameraRenderer;
import com.verr1.vscontrolcraft.blocks.chunkLoader.ChunkLoaderBlock;
import com.verr1.vscontrolcraft.blocks.chunkLoader.ChunkLoaderBlockEntity;
import com.verr1.vscontrolcraft.blocks.jointMotor.JointMotorBlock;
import com.verr1.vscontrolcraft.blocks.jointMotor.JointMotorBlockEntity;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetBlock;
import com.verr1.vscontrolcraft.blocks.magnet.MagnetBlockEntity;
import com.verr1.vscontrolcraft.blocks.pivotJoint.PivotJointBlock;
import com.verr1.vscontrolcraft.blocks.pivotJoint.PivotJointBlockEntity;
import com.verr1.vscontrolcraft.blocks.revoluteJoint.RevoluteJointBlock;
import com.verr1.vscontrolcraft.blocks.revoluteJoint.RevoluteJointBlockEntity;
import com.verr1.vscontrolcraft.blocks.servoMotor.ServoMotorBlock;
import com.verr1.vscontrolcraft.blocks.servoMotor.ServoMotorBlockEntity;
import com.verr1.vscontrolcraft.blocks.servoMotor.ServoMotorRenderer;
import com.verr1.vscontrolcraft.blocks.slider.SliderControllerBlock;
import com.verr1.vscontrolcraft.blocks.slider.SliderControllerBlockEntity;
import com.verr1.vscontrolcraft.blocks.slider.SliderRenderer;
import com.verr1.vscontrolcraft.blocks.sphericalHinge.SphericalHingeBlock;
import com.verr1.vscontrolcraft.blocks.sphericalHinge.SphericalHingeBlockEntity;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerBlock;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerBlockEntity;
import com.verr1.vscontrolcraft.blocks.spinalyzer.SpinalyzerRenderer;
import com.verr1.vscontrolcraft.blocks.wingController.WingControllerBlock;
import com.verr1.vscontrolcraft.blocks.wingController.WingControllerBlockEntity;
import com.verr1.vscontrolcraft.blocks.recevier.ReceiverBlock;
import com.verr1.vscontrolcraft.blocks.recevier.ReceiverBlockEntity;
import com.verr1.vscontrolcraft.blocks.transmitter.TransmitterBlock;
import com.verr1.vscontrolcraft.blocks.transmitter.TransmitterBlockEntity;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerBlock;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerBlockEntity;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerBlock;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerBlockEntity;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerInstance;
import com.verr1.vscontrolcraft.blocks.propellerController.PropellerControllerRenderer;
import com.verr1.vscontrolcraft.blocks.propeller.PropellerRenderer;
import com.verr1.vscontrolcraft.blocks.wingController.WingControllerRenderer;

import static com.verr1.vscontrolcraft.ControlCraft.REGISTRATE;

public class AllBlockEntities {
    public static final BlockEntityEntry<ChunkLoaderBlockEntity> CHUNK_LOADER_BLOCKENTITY = REGISTRATE
            .blockEntity(ChunkLoaderBlock.ID, ChunkLoaderBlockEntity::new)
            .validBlock(AllBlocks.CHUNK_LOADER)
            .register();

    public static final BlockEntityEntry<PropellerControllerBlockEntity> PROPELLER_CONTROLLER_BLOCKENTITY = REGISTRATE
            .blockEntity(PropellerControllerBlock.ID, PropellerControllerBlockEntity::new)
            .instance(()-> PropellerControllerInstance::new)
            .validBlock(AllBlocks.PROPELLER_CONTROLLER)
            .renderer(() -> PropellerControllerRenderer::new)
            .register();

    public static final BlockEntityEntry<PropellerBlockEntity> PROPELLER_BLOCKENTITY = REGISTRATE
            .blockEntity(PropellerBlock.ID, PropellerBlockEntity::new)
            .validBlock(AllBlocks.PROPELLER_BLOCK)
            .renderer(() -> PropellerRenderer::new)
            .register();

    public static final BlockEntityEntry<TransmitterBlockEntity> TRANSMITTER_BLOCKENTITY = REGISTRATE
            .blockEntity(TransmitterBlock.ID, TransmitterBlockEntity::new)
            .validBlock(AllBlocks.TRANSMITTER_BLOCK)
            .register();

    public static final BlockEntityEntry<ReceiverBlockEntity> RECEIVER_BLOCKENTITY = REGISTRATE
            .blockEntity(ReceiverBlock.ID, ReceiverBlockEntity::new)
            .validBlock(AllBlocks.RECEIVER_BLOCK)
            .register();

    public static final BlockEntityEntry<WingControllerBlockEntity> WING_CONTROLLER_BLOCKENTITY = REGISTRATE
            .blockEntity(WingControllerBlock.ID, WingControllerBlockEntity::new)
            .validBlock(AllBlocks.WING_CONTROLLER_BLOCK)
            .renderer(() -> WingControllerRenderer::new)
            .register();

    public static final BlockEntityEntry<SpinalyzerBlockEntity> SPINALYZER_BLOCKENTITY = REGISTRATE
            .blockEntity(SpinalyzerBlock.ID, SpinalyzerBlockEntity::new)
            .validBlock(AllBlocks.SPINALYZER_BLOCK)
            .renderer(() -> SpinalyzerRenderer::new)
            .register();

    public static final BlockEntityEntry<CameraBlockEntity> CAMERA_BLOCKENTITY = REGISTRATE
            .blockEntity(CameraBlock.ID, CameraBlockEntity::new)
            .validBlock(AllBlocks.CAMERA_BLOCK)
            .renderer(()-> CameraRenderer::new)
            .register();

    public static final BlockEntityEntry<ServoMotorBlockEntity> SERVO_MOTOR_BLOCKENTITY = REGISTRATE
            .blockEntity(ServoMotorBlock.ID, ServoMotorBlockEntity::new)
            .validBlock(AllBlocks.SERVO_MOTOR_BLOCK)
            .renderer(()-> ServoMotorRenderer::new)
            .register();

    public static final BlockEntityEntry<MagnetBlockEntity> MAGNET_BLOCKENTITY = REGISTRATE
            .blockEntity(MagnetBlock.ID, MagnetBlockEntity::new)
            .validBlock(AllBlocks.MAGNET_BLOCK)
            .register();

    public static final BlockEntityEntry<JointMotorBlockEntity> JOINT_MOTOR_BLOCKENTITY = REGISTRATE
            .blockEntity(JointMotorBlock.ID, JointMotorBlockEntity::new)
            .validBlock(AllBlocks.JOINT_MOTOR_BLOCK)
            .register();

    public static final BlockEntityEntry<SphericalHingeBlockEntity> SPHERE_HINGE_BLOCKENTITY = REGISTRATE
            .blockEntity(SphericalHingeBlock.ID, SphericalHingeBlockEntity::new)
            .validBlock(AllBlocks.SPHERE_HINGE_BLOCK)
            .register();

    public static final BlockEntityEntry<RevoluteJointBlockEntity> REVOLUTE_JOINT_BLOCKENTITY = REGISTRATE
            .blockEntity(RevoluteJointBlock.ID, RevoluteJointBlockEntity::new)
            .validBlock(AllBlocks.REVOLUTE_JOINT_BLOCK)
            .register();

    public static final BlockEntityEntry<PivotJointBlockEntity> PIVOT_JOINT_BLOCKENTITY = REGISTRATE
            .blockEntity(PivotJointBlock.ID, PivotJointBlockEntity::new)
            .validBlock(AllBlocks.PIVOT_JOINT_BLOCK)
            .register();

    public static final BlockEntityEntry<SliderControllerBlockEntity> SLIDER_CONTROLLER_BLOCKENTITY = REGISTRATE
            .blockEntity(SliderControllerBlock.ID, SliderControllerBlockEntity::new)
            .validBlock(AllBlocks.SLIDER_CONTROLLER_BLOCK)
            .renderer(() -> SliderRenderer::new)
            .register();

    public static void register(){

    }
}
