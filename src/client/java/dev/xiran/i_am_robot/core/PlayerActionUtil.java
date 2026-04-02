package dev.xiran.i_am_robot.core;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;

public class PlayerActionUtil {

    public static LinkedList<Component> messageQueue = new LinkedList<>();

    public static void sendClientMessage(Component component) {
        messageQueue.addLast(component);
    }

    // 在非 Render Thread 发送消息会莫名其妙抛异常，所以只能让 Render Thread 在 tick 时发送
    public static void sendMessageOnTick(Minecraft mc) {
        if (!messageQueue.isEmpty()) {
            mc.gui.getChat().addMessage(messageQueue.poll());
        }
    }
}
