package com.verr1.vscontrolcraft.registry;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.Create;
import com.verr1.vscontrolcraft.ControlCraft;

public class AllPartialModels {
    public static final PartialModel
        NORMAL_PROPELLER = block("simple_propeller"),
        SPINALYZR_AXES = block("spinalyzer_axes") ,
        CAMERA_LENS = block("camera_lens");


    private static PartialModel block(String path) {
        return new PartialModel(ControlCraft.asResource("block/" + path));
    }

    public static void init() {
        // init static fields
    }
}
