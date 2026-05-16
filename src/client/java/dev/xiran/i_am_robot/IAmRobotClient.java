package dev.xiran.i_am_robot;

import dev.xiran.i_am_robot.command.ModCommand;
import dev.xiran.i_am_robot.player.ContainerUtil;
import dev.xiran.i_am_robot.player.PlayerActionUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import java.io.File;

public class IAmRobotClient implements ClientModInitializer {

	public static File scriptDir;
	public static File dataDir;

	@Override
	public void onInitializeClient() {
		File configDir = new File(Minecraft.getInstance().gameDirectory, "config\\i_am_robot");
		scriptDir = new File(configDir, "scripts");
		dataDir = new File(configDir, "data");
		initDirectory(scriptDir);
		initDirectory(dataDir);

		ModCommand.initialize();

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
	}

	public void onClientTick(Minecraft mc) {
		PlayerActionUtil.sendMessageOnTick(mc);
		ContainerUtil.handleCloseContainer(mc);
	}

	private void initDirectory(File dir) {
		if (!dir.exists()) {
			if (!dir.mkdirs()) IAmRobot.LOGGER.error("Failed to create directory: {}", dir);
		}
	}
}