package com.verr1.controlcraft.foundation.type.descriptive;

import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.utils.LangUtils;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.verr1.controlcraft.utils.ComponentUtils.literals;

public enum CameraClipType implements Descriptive<CameraClipType> {



    RAY_ALWAYS(
            Component.literal("ALWAYS"),
            literals(
                    "Always Cast Ray"
            )
    ),

    RAY_ON_USE(
            Component.literal("ON USE"),
            literals(
                    "Cast Ray Only When Being Used"
            )
    ),

    NO_RAY(
            Component.literal("NEVER"),
            literals(
                    "Doesn't Cast Ray"
            )
    ),



    SHIP_CLIP_ON(
            Component.literal("ON"),
            literals(
                    "Outlines Ship"
            )
    ),
    SHIP_CLIP_OFF(
            Component.literal("OFF"),
            literals(
                    "Doesn't Outline Ship"
            )
    ),


    ENTITY_TARGETED_ONLY(
            Component.literal("TARGET ONLY"),
            literals(
                    "Only Outline Target Entities"
            )
    ),

    ENTITY_IN_VIEW(
            Component.literal("IN VIEW"),
            literals(
                    "Outline All Entity In View"
            )
    ),

    ENTITY_NEAREST(
            Component.literal("NEAREST"),
            literals(
                    "Outline Nearest Entity In View"
            )
    ),

    ENTITY_OFF(
            Component.literal("OFF"),
            literals(
                    "Doesn't Outline Entities"
            )
    ),


    ;

    public static final CameraClipType[] RAY = new CameraClipType[]{RAY_ALWAYS, RAY_ON_USE, NO_RAY};
    public static final CameraClipType[] SHIP = new CameraClipType[]{SHIP_CLIP_ON, SHIP_CLIP_OFF};
    public static final CameraClipType[] ENTITY = new CameraClipType[]{ENTITY_TARGETED_ONLY, ENTITY_IN_VIEW, ENTITY_OFF, ENTITY_NEAREST};


    CameraClipType(
            Component name,
            List<Component> description
    ){
        LangUtils.registerDefaultName(CameraClipType.class, this, name);
        LangUtils.registerDefaultDescription(CameraClipType.class, this, description);
    }

    CameraClipType(
        List<Component> description
    ){
        LangUtils.registerDefaultName(CameraClipType.class, this, Component.literal(name()));
        LangUtils.registerDefaultDescription(CameraClipType.class, this, description);
    }

    @Override
    public CameraClipType self() {
        return this;
    }

    @Override
    public Class<CameraClipType> clazz() {
        return CameraClipType.class;
    }

    public static void register(){
        LangUtils.registerDefaultDescription(CameraClipType.class, literals("Camera Clip Settings"));
        // load by class loader and constructors will call registerDefaultName etc
    }
}
