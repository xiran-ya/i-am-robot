package dev.xiran.i_am_robot;

import net.fabricmc.api.ClientModInitializer;

public class IAmRobotClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		IAmRobot.LOGGER.info("Hello qwq");
	}
}