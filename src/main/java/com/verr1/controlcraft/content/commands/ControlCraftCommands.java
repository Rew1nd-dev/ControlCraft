package com.verr1.controlcraft.content.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.verr1.controlcraft.ControlCraft;
import com.verr1.controlcraft.ControlCraftServer;
import com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies;
import com.verr1.controlcraft.registry.ControlCraftAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = ControlCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ControlCraftCommands {

    private static void clearAllAttachments(){
        ControlCraftServer
                .INSTANCE
                .getAllLevels()
                .forEach(
                    lvl -> ValkyrienSkies
                            .getShipWorld(lvl)
                            .getAllShips()
                            .forEach(
                                s -> Arrays
                                        .stream(ControlCraftAttachments.values())
                                        .map(ControlCraftAttachments::getClazz)
                                        .filter(c -> s.getAttachment(c) != null)
                                        .forEach(
                                                c -> s.saveAttachment(c, null)
                                        )
                            )
                );
    }

    public static void registerServerCommands(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(
            Commands
                .literal("controlcraft")
                .then(
                    LiteralArgumentBuilder.<CommandSourceStack>literal("clear-attachment")
                        .executes(
                                context -> {
                                    clearAllAttachments();
                                    return 1;
                        })
                )
        );
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        registerServerCommands(event.getDispatcher());
    }

}
