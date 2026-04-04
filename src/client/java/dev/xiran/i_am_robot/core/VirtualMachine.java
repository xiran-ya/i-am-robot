package dev.xiran.i_am_robot.core;

import dev.xiran.i_am_robot.IAmRobot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VirtualMachine implements Runnable {
    static final VirtualMachine INSTANCE = new VirtualMachine();
    private boolean running = false;
    private File script;
    private final String[] instructions = new String[1024];
    private int programCounter;
    HashMap<String, Object> variableTable = new HashMap<>();

    public static final Pattern commentPattern = Pattern.compile("//");

    @Override
    public void run() {
        running = true;
        programCounter = 0;
        Arrays.fill(instructions, null);
        try {
            try {
                readScript();
            } catch (FileNotFoundException e) {
                IAmRobot.LOGGER.error("Cannot find script file", e);
                return;
            }

            try {
                while (AssemblerParser.evaluate(instructions[programCounter])) {
                    programCounter++;
                }
            } catch (Exception e) {
                PlayerActionUtil.sendClientMessage(Component.translatable("message.i_am_robot.vm.exception").withStyle(ChatFormatting.RED));
                String message = e.getClass().getSimpleName() + ": " + e.getMessage();
                PlayerActionUtil.sendClientMessage(Component.literal(message).withStyle(ChatFormatting.RED));
                PlayerActionUtil.sendClientMessage(Component.literal("at <main>: " + programCounter).withStyle(ChatFormatting.RED));
            }
        } finally {
            variableTable.clear();
            running = false;
        }
    }

    private void readScript() throws FileNotFoundException {
        try (Scanner sc = new Scanner(script)) {
            int i = 0;
            while (sc.hasNextLine()) {
                String s = sc.nextLine();

                Matcher matcher = commentPattern.matcher(s);
                if (matcher.find()) {
                    s = s.substring(0, matcher.start());
                }

                if (s.isBlank()) continue;
                instructions[i] = s.trim();
                i++;
            }
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

    /**
     * 跳转到指定的位置
     * @param n 跳转后执行的下一条指令
     * @throws IllegalArgumentException 若 n < 0
     */
    public void jump(int n) {
        if (n < 0) throw new IllegalArgumentException("Cannot jump to address that are less than 0");
        programCounter = n - 1;
    }

    public void createVariable(String name, Object value) {
        variableTable.put(name, value);
    }

    public Object getVariable(String name) {
        Object value = variableTable.get(name);
        if (value == null) throw new VMRuntimeException(String.format("Variable \"%s\" is not defined", name));
        return value;
    }

    public void setVariable(String name, Object newValue) {
        Object o = variableTable.replace(name, newValue);
        if (o == null) throw new VMRuntimeException(String.format("Variable \"%s\" is not defined", name));
    }
}
