package com.verr1.controlcraft.config;

import com.verr1.controlcraft.ControlCraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = ControlCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockPropertyConfig
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue CC_OVERCLOCKING = BUILDER
            .comment(
                    "------------------------------------",
                    "  Warning: Experimental",
                    "------------------------------------",
                    "  By Default, ComputerCraft Is Running at Game Thread. ",
                    "  When Enable This Settings, ComputerCraft Will Run at Another Thread Which Is Synced By VS Physics Thread. ",
                    "  This Feature Is Currently **Experimental**, It Might Cause Unknown Concurrent Issues")
            .define("Enable Physics Thread Synced ComputerCraft", false);

    private static final ForgeConfigSpec.IntValue MAX_DISTANCE_SPATIAL_CAN_LINK = BUILDER
            .comment(
                    "  Defines How Long Can Running-Dynamic Spatial Can Find A Running-Static One As It's Target")
            .defineInRange("Max Distance Spatial Can Link", 256, 1, 1024);

    private static final ForgeConfigSpec.IntValue CHUNK_LOADER_RADIUS = BUILDER
            .comment(
                    "  Defines The Square Radius Of Chunk Loader Loading Spec")
            .defineInRange("Chunk Loader Radius", 2, 1, 64);


    private static final ForgeConfigSpec.BooleanValue NO_NEGATIVE_PID_INPUT = BUILDER
            .comment(
                    "  Negative Input Usually Cause Positive Feedback And Make Things Goes Crazy",
                    "  When Enabled, The PID Controller Will Reverse Negative Input")
            .define("No Negative PID Input", true);

    private static final ForgeConfigSpec.IntValue PHYSICS_MAX_SLIDE_DISTANCE = BUILDER
            .comment(
                    "  Defines How Far The Slide Constraint Can Slide",
                    "  A Farther Distance Is Unstable In Some Cases")
            .defineInRange("Physics Piston Max Slide Distance", 32, 0, 1024);


    private static final ForgeConfigSpec.IntValue PROPELLER_MAX_THRUST = BUILDER
            .comment(
                    "  Defines The Maximum Force A Single Propeller Can Apply"
                    )
            .defineInRange("Propeller Max Force", 3_000_000, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue PROPELLER_MAX_TORQUE = BUILDER
            .comment(
                    "  Defines The Maximum Torque A Single Propeller Can Apply"
            )
            .defineInRange("Propeller Max Torque", 3_000_000, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue JET_MAX_THRUST = BUILDER
            .comment(
                    "  Defines The Maximum Force A Single Jet Engine Can Apply"
            )
            .defineInRange("Jet Max Force", 3_000_000, 0, Integer.MAX_VALUE);



    public static final ForgeConfigSpec SPEC = BUILDER.build();



    public static boolean _CC_OVERCLOCKING;

    public static boolean _NO_NEGATIVE_PID_INPUT;

    public static int _CHUNK_LOADER_RADIUS;

    public static int _MAX_DISTANCE_SPATIAL_CAN_LINK;

    public static int _PHYSICS_MAX_SLIDE_DISTANCE;

    public static int _PROPELLER_MAX_THRUST;

    public static int _PROPELLER_MAX_TORQUE;

    public static int _JET_MAX_THRUST;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        _CC_OVERCLOCKING = CC_OVERCLOCKING.get();
        _NO_NEGATIVE_PID_INPUT = NO_NEGATIVE_PID_INPUT.get();
        _CHUNK_LOADER_RADIUS = CHUNK_LOADER_RADIUS.get();
        _PHYSICS_MAX_SLIDE_DISTANCE = PHYSICS_MAX_SLIDE_DISTANCE.get();
        _MAX_DISTANCE_SPATIAL_CAN_LINK = MAX_DISTANCE_SPATIAL_CAN_LINK.get();
        _PROPELLER_MAX_THRUST = PROPELLER_MAX_THRUST.get();
        _PROPELLER_MAX_TORQUE = PROPELLER_MAX_TORQUE.get();
        _JET_MAX_THRUST = JET_MAX_THRUST.get();
    }
}
