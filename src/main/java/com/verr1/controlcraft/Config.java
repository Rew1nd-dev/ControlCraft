package com.verr1.controlcraft;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = ControlCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue CC_OVERCLOCKING = BUILDER
            .comment(
                    "------------------------------------",
                    "  Warning: Experimental, May Break Saves !!!",
                    "------------------------------------",
                    "  By Default, ComputerCraft Is Running at Game Thread. ",
                    "  When Enable This Settings, ComputerCraft Will Run at Another Thread Which Is Synced By VS Physics Thread. ",
                    "  This Feature Is Currently **Experimental**, It Might Cause Unknown Concurrent Issues")
            .define("Enable Physics Thread Synced ComputerCraft", false);

    private static final ForgeConfigSpec.IntValue MAX_DISTANCE_SPATIAL_CAN_LINK = BUILDER
            .comment(
                    "  Defines How Long Can Running-Dynamic Spatial Can Find A Running-Static One As It's Target")
            .defineInRange("Max Distance Spatial Can Link", 128, 1, 1024);

    private static final ForgeConfigSpec.IntValue CHUNK_LOADER_RADIUS = BUILDER
            .comment(
                    "  Defines The Square Radius Of Chunk Loader Loading Spec")
            .defineInRange("Chunk Loader Radius", 2, 1, 16);


    private static final ForgeConfigSpec.BooleanValue NO_NEGATIVE_PID_INPUT = BUILDER
            .comment(
                    "  Negative Input Usually Cause Positive Feedback And Make Things Goes Crazy",
                    "  When Enabled, The PID Controller Will Reverse Negative Input")
            .define("No Negative PID Input", false);

    private static final ForgeConfigSpec.IntValue PHYSICS_MAX_SLIDE_DISTANCE = BUILDER
            .comment(
                    "  Defines How Far The Slide Constraint Can Slide",
                    "  A Farther Distance Is Unstable In Some Cases")
            .defineInRange("Physics Piston Max Slide Distance", 32, 0, 1024);


    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean OVERCLOCK_COMPUTERCRAFT = false;
    public static int MaxDistanceSpatialCanLink = 64;
    public static int ChunkLoaderRadius = 2;
    public static boolean EnableAnnihilator = false;
    public static boolean NoNegativePIDInput = false;
    public static int PhysicsMaxSlideDistance = 32;



    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        OVERCLOCK_COMPUTERCRAFT = CC_OVERCLOCKING.get();
        MaxDistanceSpatialCanLink = MAX_DISTANCE_SPATIAL_CAN_LINK.get();
        ChunkLoaderRadius = CHUNK_LOADER_RADIUS.get();
        NoNegativePIDInput = NO_NEGATIVE_PID_INPUT.get();
        PhysicsMaxSlideDistance = PHYSICS_MAX_SLIDE_DISTANCE.get();
    }
}
