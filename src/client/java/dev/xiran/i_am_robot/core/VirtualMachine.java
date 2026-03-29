package dev.xiran.i_am_robot.core;

import dev.xiran.i_am_robot.IAmRobot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class VirtualMachine implements Runnable {
    private static final VirtualMachine INSTANCE = new VirtualMachine();
    private boolean running = false;
    private File script;
    private final String[] instructions = new String[1024];

    @Override
    public void run() {
        running = true;
        try {
            Arrays.fill(instructions, null);
            try (Scanner sc = new Scanner(script)) {
                int i = 0;
                while (sc.hasNextLine()) {
                    String s = sc.nextLine();
                    if (s.isBlank()) continue;
                    instructions[i] = s;
                    i++;
                }
            } catch (FileNotFoundException e) {
                IAmRobot.LOGGER.error("Cannot find the script file", e);
                // TODO: 向聊天栏发送信息
                running = false;
                return;
            }

            for (String instruction : instructions) {
                if (instruction == null) break;
                IAmRobot.LOGGER.info(instruction);
            }
        } finally {
            running = false;
        }
    }

    public static VirtualMachine getInstance() {
        return INSTANCE;
    }

    public boolean isRunning() {
        return running;
    }

    public void setScript(File script) {
        this.script = script;
    }
}
