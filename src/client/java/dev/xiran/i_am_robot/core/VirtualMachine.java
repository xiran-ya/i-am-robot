package dev.xiran.i_am_robot.core;

import dev.xiran.i_am_robot.IAmRobot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class VirtualMachine implements Runnable {
    static final VirtualMachine INSTANCE = new VirtualMachine();
    private boolean running = false;
    private File script;
    private final String[] instructions = new String[1024];
    private int programCounter;
    HashMap<String, Object> variableTable = new HashMap<>();

    @Override
    public void run() {
        running = true;
        programCounter = 0;
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
                running = false;
                return;
            }

            try {
                while (AssemblerParser.evaluate(instructions[programCounter])) {
                    programCounter++;
                }
            } catch (Exception e) {
                // TODO: 异常处理
                e.printStackTrace();
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
