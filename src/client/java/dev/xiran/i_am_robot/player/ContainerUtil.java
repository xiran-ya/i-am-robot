package dev.xiran.i_am_robot.player;

import dev.xiran.i_am_robot.core.VMRuntimeException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.locks.ReentrantLock;

public class ContainerUtil {

    public static final int MOUSE_LEFT = 0;
    public static final int MOUSE_RIGHT = 1;

    static boolean shouldCloseContainer;
    static ReentrantLock containerLock = new ReentrantLock();

    // handleInventoryMouseClick(int containerId, int slotIndex, int mouseButton, ClickType clickType, Player player)

    /**
     * 从容器中获取指定数量物品，放到指定物品栏，然后关闭容器。如果没有打开容器界面，则什么都不做
     * @param count 要取的物品数量，不能超过物品的最大堆叠数
     * @param inventorySlot 物品要放到的物品栏槽位编号，左上为 0，往右下递增
     */
    public static void getItem(int count, int inventorySlot) {
        if (count > 64) throw new IllegalArgumentException("Cannot get 65 or more item at once");
        LocalPlayer player = Minecraft.getInstance().player;
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (player != null && gameMode != null && player.hasContainerOpen()) {
            AbstractContainerMenu menu = player.containerMenu;
            int inventoryIndex = mapInventorySlot2Index(menu, inventorySlot);

            int loopCounter = 0;
            while (true) {
                if (loopCounter > 64) throw new VMRuntimeException("Too many loops taken to get item. Interrupting.");  // 防止死循环
                NonNullList<ItemStack> items = menu.getItems();
                int itemIndex = findAvailableItem(items);
                gameMode.handleInventoryMouseClick(menu.containerId, itemIndex, MOUSE_LEFT, ClickType.PICKUP, player);
                int carriedCount = menu.getCarried().getCount();
                if (carriedCount >= count) {
                    for (int i = 0; i < carriedCount - count; i++) {
                        gameMode.handleInventoryMouseClick(menu.containerId, itemIndex, MOUSE_RIGHT, ClickType.PICKUP, player);
                    }
                    gameMode.handleInventoryMouseClick(menu.containerId, inventoryIndex, MOUSE_LEFT, ClickType.PICKUP, player);
                    closeContainer();
                    return;
                } else {
                    gameMode.handleInventoryMouseClick(menu.containerId, inventoryIndex, MOUSE_LEFT, ClickType.PICKUP, player);
                    count = count - carriedCount;
                }
                loopCounter++;
            }
        }
    }

    /**
     * 将物品栏中的一组物品转移到容器中
     * @param inventorySlot 要转移的物品栏编号
     * @see ContainerUtil#getItem(int, int)
     */
    public static void putItem(int inventorySlot) {
        LocalPlayer player = Minecraft.getInstance().player;
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (player != null && gameMode != null && player.hasContainerOpen()) {
            AbstractContainerMenu menu = player.containerMenu;
            int inventoryIndex = mapInventorySlot2Index(menu, inventorySlot);
            gameMode.handleInventoryMouseClick(menu.containerId, inventoryIndex, MOUSE_LEFT, ClickType.QUICK_MOVE, player);
            closeContainer();
        }
    }

    private static int findAvailableItem(NonNullList<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                return i;
            }
        }
        throw new VMRuntimeException("Cannot find any item in container");
    }

    private static int mapInventorySlot2Index(AbstractContainerMenu menu, int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot > 35) throw new IllegalArgumentException("Invalid inventory slot index");
        int slotCount = menu.slots.size();
        return inventorySlot + slotCount - 36;
    }

    public static void closeContainer() {
        containerLock.lock();
        try {
            shouldCloseContainer = true;
        } finally {
            containerLock.unlock();
        }

        try {
            Thread.sleep(50L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void handleCloseContainer(Minecraft mc) {
        containerLock.lock();
        try {
            if (shouldCloseContainer) {
                LocalPlayer player = mc.player;
                if (player != null) {
                    player.closeContainer();
                }
                shouldCloseContainer = false;
            }
        } finally {
            containerLock.unlock();
        }
    }
}
