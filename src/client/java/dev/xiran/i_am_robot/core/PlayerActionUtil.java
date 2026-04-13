package dev.xiran.i_am_robot.core;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
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

    public static void attack() {
        KeyMapping keyAttack = Minecraft.getInstance().options.keyAttack;
        try {
            Field clickCount = KeyMapping.class.getDeclaredField("clickCount");
            clickCount.setAccessible(true);
            clickCount.setInt(keyAttack, 1);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void use() {
        KeyMapping keyAttack = Minecraft.getInstance().options.keyUse;
        try {
            Field clickCount = KeyMapping.class.getDeclaredField("clickCount");
            clickCount.setAccessible(true);
            clickCount.setInt(keyAttack, 1);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setHoldAttack(boolean state) {
        Minecraft.getInstance().options.keyAttack.setDown(state);
    }

    public static void setHoldUse(boolean state) {
        Minecraft.getInstance().options.keyUse.setDown(state);
    }

    public static void hotbar(int i) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.getInventory().setSelectedSlot(i);
        }
    }

    /**
     * Rotation (set yaw & pitch)
     */
    public static void rot(double yaw, double pitch) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.setXRot((float) yaw);
            player.setYRot((float) pitch);
        }
    }
}
