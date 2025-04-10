package com.verr1.controlcraft.content.gui.layouts.api;

import com.verr1.controlcraft.foundation.data.control.KinematicController;
import com.verr1.controlcraft.foundation.type.descriptive.TargetMode;
import org.joml.Vector3dc;

public interface IKinematicUIDevice {

    KinematicController getController();

    TargetMode getTargetMode();

    void setTargetMode(TargetMode mode);

    double getCompliance();

    void setCompliance(double d);

    Vector3dc getOffset();

    void setOffset(Vector3dc offset);


}
