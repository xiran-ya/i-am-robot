package dev.xiran.i_am_robot.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.xiran.i_am_robot.IAmRobotClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.IOException;

public class ModCommand {
    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("bot")
                .then(ClientCommandManager.literal("list")
                    .executes(ModCommand::listRoot)
                    .then(ClientCommandManager.argument("path", StringArgumentType.greedyString())
                        .executes(ModCommand::listWithPath)
                    )
                )
            );
        });
    }

    public static int list(CommandContext<FabricClientCommandSource> context, String path) {
        File currentPath = (path == null) ? IAmRobotClient.scriptDir : new File(IAmRobotClient.scriptDir, path);
            String[] files = currentPath.list();
            if (files == null) {
                context.getSource().sendError(Component.translatable("command_feedback.i_am_robot.io_exception"));
                return 0;
            }
            context.getSource().sendFeedback(Component.literal("============").withStyle(ChatFormatting.GRAY));
            for (String file : files) {
                context.getSource().sendFeedback(Component.literal(file).withStyle(ChatFormatting.GRAY));
            }
            return 1;
    }
    public static int listRoot(CommandContext<FabricClientCommandSource> context) {
        return list(context, null);
    }
    public static int listWithPath(CommandContext<FabricClientCommandSource> context) {
        return list(context, StringArgumentType.getString(context, "path"));
    }


}
