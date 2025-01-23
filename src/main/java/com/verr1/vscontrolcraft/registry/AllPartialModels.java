package com.verr1.vscontrolcraft.registry;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.Create;
import com.verr1.vscontrolcraft.ControlCraft;

public class AllPartialModels {
    public static final PartialModel
        NORMAL_PROPELLER = block("simple_propeller"),
        SPINALYZR_AXES = block("spinalyzer_axes") ,
        SERVO_TOP = block("servo_top"),
        WING_CONTROLLER_TOP = block("wing_controller_top"),
        CAMERA_LENS = block("camera_lens"),

        SPHERICAL_HINGE_BASE = block("sphere_hinge/sph_base"),
        SPHERICAL_HINGE_HALF = block("sphere_hinge/sph_half"),
        SPHERICAL_HINGE_3QUARTER = block("sphere_hinge/sph_3quarter"),
        SPHERICAL_HINGE_FULL = block("sphere_hinge/block");




    private static PartialModel block(String path) {
        return new PartialModel(ControlCraft.asResource("block/" + path));
    }


    public static void init() {
        // init static fields
    }
}
