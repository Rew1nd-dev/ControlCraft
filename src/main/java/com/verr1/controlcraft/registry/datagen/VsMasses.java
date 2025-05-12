package com.verr1.controlcraft.registry.datagen;

import com.verr1.controlcraft.content.blocks.anchor.AnchorBlock;
import com.verr1.controlcraft.content.blocks.camera.CameraBlock;
import com.verr1.controlcraft.content.blocks.flap.FlapBearingBlock;
import com.verr1.controlcraft.content.blocks.jet.JetBlock;
import com.verr1.controlcraft.content.blocks.jet.JetRudderBlock;
import com.verr1.controlcraft.content.blocks.joints.FreeJointBlock;
import com.verr1.controlcraft.content.blocks.joints.PivotJointBlock;
import com.verr1.controlcraft.content.blocks.joints.RevoluteJointBlock;
import com.verr1.controlcraft.content.blocks.kinetic.proxy.KineticProxyBlock;
import com.verr1.controlcraft.content.blocks.loader.ChunkLoaderBlock;
import com.verr1.controlcraft.content.blocks.motor.*;
import com.verr1.controlcraft.content.blocks.propeller.PropellerBlock;
import com.verr1.controlcraft.content.blocks.propeller.PropellerControllerBlock;
import com.verr1.controlcraft.content.blocks.receiver.PeripheralInterfaceBlock;
import com.verr1.controlcraft.content.blocks.slider.DynamicSliderBlock;
import com.verr1.controlcraft.content.blocks.slider.KinematicSliderBlock;
import com.verr1.controlcraft.content.blocks.spatial.SpatialAnchorBlock;
import com.verr1.controlcraft.content.blocks.spinalyzer.SpinalyzerBlock;
import com.verr1.controlcraft.content.blocks.terminal.TerminalBlock;
import com.verr1.controlcraft.content.blocks.transmitter.PeripheralProxyBlock;

public enum VsMasses {

    ANCHOR(AnchorBlock.ID),
    CAMERA(CameraBlock.ID, 5),
    FLAP_BEARING(FlapBearingBlock.ID),
    JET_ENGINE(JetBlock.ID, 500),
    JET_RUDDER(JetRudderBlock.ID, 5),
    REVOLUTE_JOINT(RevoluteJointBlock.ID, 5),
    FREE_JOINT(FreeJointBlock.ID, 5),
    PIVOT_JOINT(PivotJointBlock.ID, 5),
    KINETIC_PROXY(KineticProxyBlock.ID),
    CHUNK_LOADER(ChunkLoaderBlock.ID),
    D_R_MOTOR(DynamicRevoluteMotorBlock.ID),
    D_J_MOTOR(DynamicJointMotorBlock.ID),
    K_R_MOTOR(KinematicRevoluteMotorBlock.ID),
    K_J_MOTOR(KinematicJointMotorBlock.ID),
    D_SLIDER(DynamicSliderBlock.ID),
    K_SLIDER(KinematicSliderBlock.ID),
    PROPELLER_CONTROLLER(PropellerControllerBlock.ID, 50),
    PROPELLER(PropellerBlock.ID, 5),
    RECEIVER(PeripheralInterfaceBlock.ID, 5),
    TRANSMITTER(PeripheralProxyBlock.ID, 5),
    SPATIAL_ANCHOR(SpatialAnchorBlock.ID, 500),
    REDSTONE_TERMINAL(TerminalBlock.ID, 5),
    SPINAL(SpinalyzerBlock.ID, 5),;

    public final String ID;
    public final double mass;

    VsMasses(String ID, double mass) {
        this.ID = ID;
        this.mass = mass;
    }

    VsMasses(String ID) {
        this.ID = ID;
        this.mass = 100;
    }
}
