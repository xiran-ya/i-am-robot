package dev.xiran.i_am_robot.core;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

public class PlayerActionUtil {

    public static Deque<Component> messageQueue = new ArrayDeque<>();
    private static final ReentrantLock queueLock = new ReentrantLock();

    public static void sendClientMessage(Component component) {
        queueLock.lock();
        try {
            messageQueue.addLast(component);
        } finally {
            queueLock.unlock();
        }
    }

    // 在非 Render Thread 发送消息会莫名其妙抛异常，所以只能让 Render Thread 在 tick 时发送
    public static void sendMessageOnTick(Minecraft mc) {
        queueLock.lock();
        try {
            if (!messageQueue.isEmpty()) {
                //noinspection DataFlowIssue
                mc.gui.getChat().addMessage(messageQueue.poll());
            }
        } finally {
            queueLock.unlock();
        }
    }
}
