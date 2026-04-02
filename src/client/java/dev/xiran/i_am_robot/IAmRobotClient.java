package dev.xiran.i_am_robot;

import dev.xiran.i_am_robot.command.ModCommand;
import dev.xiran.i_am_robot.core.PlayerActionUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import java.io.File;

public class IAmRobotClient implements ClientModInitializer {

	public static File scriptDir;

	@Override
	public void onInitializeClient() {
		IAmRobot.LOGGER.info("Hello qwq"); // TODO: Remove before release

		File configDir = new File(Minecraft.getInstance().gameDirectory, "config\\i_am_robot");
		scriptDir = new File(configDir, "scripts");
		if (!configDir.exists()) {
			if (!configDir.mkdirs()) IAmRobot.LOGGER.error("Failed to create directory: {}", configDir);
			if (!scriptDir.mkdir()) IAmRobot.LOGGER.error("Fail to create directory: {}", scriptDir);
		}

		ModCommand.initialize();

		ClientTickEvents.END_CLIENT_TICK.register(PlayerActionUtil::sendMessageOnTick);

	}
}