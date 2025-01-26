package com.verr1.vscontrolcraft;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.verr1.vscontrolcraft.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;


/*
* TODO:
*    Visualization:
*    1. add tool tips for blocks, need data syncing.
*    2. render servo top part as a moving segment, rotate as angle changes
*    3. remake wing controller model, make a moving part connected with wing block
*    4.
*    Functionality:
*    1. extract ServoConstrainAssembleSchedule run() function, make it inside an class specific for ship aligning task
*    2. VS constrain serialize utilities
*    3. Make Force Inducer removing invalids by life time
*    4. Sync Animation Packet Simplify to one, Make Interface for all blocks with only one animated data
*    Features:
*    1. suicide block, or self-disassemble block
*    2.√ magnet block, implement using constrain or ShipForceInducer
*    3.√ Linker tool, configurable, multi-functional tool for Control Craft
*    4.√ Variants of bearings with different rotational behaviors
*    5. Directional Jet rudders, and rudder controller consuming liquid(optional), just like propeller controller
*    6. Piston with Sphere sphere_hinge connection
*
*    Configuration:
*    1. make more fields configurable
*/


// The value here should match an entry in the META-INF/mods.toml file
@Mod(ControlCraft.MODID)
public class ControlCraft
{

    public static final String MODID = "vscontrolcraft";

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ControlCraft.MODID);

    public ControlCraft(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addListener(this::addCreative);
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        if(FMLEnvironment.dist == Dist.CLIENT){
            modEventBus.addListener(this::clientSetup);
        }
        modEventBus.addListener(this::addCreative);

        AllCreativeTabs.register(modEventBus);
        AllBlocks.register();
        AllBlockEntities.register();
        AllItems.register();
        AllPackets.registerPackets();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ControlCraftClient::clientInit);

        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);// Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
    }



    public ControlCraft(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        REGISTRATE.registerEventListeners(modEventBus);
        modEventBus.addListener(this::addCreative);
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        if(FMLEnvironment.dist == Dist.CLIENT){
            modEventBus.addListener(this::clientSetup);
        }

        // Register ourselves for server and other game events we are interested in


        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        AllCreativeTabs.register(modEventBus);
        AllBlocks.register();
        AllBlockEntities.register();
        AllItems.register();
        AllPackets.registerPackets();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ControlCraftClient::clientInit);

        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.OverclockComputerCraft) LOGGER.info("CC OverClocked");


    }

    private void clientSetup(FMLClientSetupEvent event)
    {


    }


    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {

    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("HELLO from server starting");
    }


    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MODID, path);
    }
}
