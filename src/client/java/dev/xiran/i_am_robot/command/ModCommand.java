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
                .then(ClientCommandManager.literal("help")
                    .executes(ModCommand::help)
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
            sortFiles(currentPath, files);
            for (String f : files) {
                context.getSource().sendFeedback(Component.literal(f).withStyle(ChatFormatting.GRAY));
            }
            return 1;
    }
    public static int listRoot(CommandContext<FabricClientCommandSource> context) {
        return list(context, null);
    }
    public static int listWithPath(CommandContext<FabricClientCommandSource> context) {
        return list(context, StringArgumentType.getString(context, "path"));
    }
    private static void sortFiles(File root, String[] files) {
        for (int i = 0; i < files.length; i++) {
            File file = new File(root, files[i]);
            if (file.isDirectory()) {
                files[i] = "<dir> " + files[i];
            }
        }

        // 排序
        for (int i = 0; i < files.length - 1; i++) {
            for (int j = 0; j < files.length - i - 1; j++) {
                if (files[j].charAt(0) != '<' && files[j + 1].charAt(0) == '<') {
                    var temp = files[j];
                    files[j] = files[j + 1];
                    files[j + 1] = temp;
                }
            }
        }
    }

    public static int help(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        source.sendFeedback(Component.translatable("command.help.i_am_robot.line0").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD));
        source.sendFeedback(Component.translatable("command.help.i_am_robot.help").withStyle(ChatFormatting.GRAY));
        source.sendFeedback(Component.translatable("command.help.i_am_robot.list").withStyle(ChatFormatting.GRAY));
        return 1;
    }


}
