package dev.xiran.i_am_robot.core;

import dev.xiran.i_am_robot.IAmRobot;
import dev.xiran.i_am_robot.player.PlayerActionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VirtualMachine implements Runnable {
    static final VirtualMachine INSTANCE = new VirtualMachine();
    private boolean running = false;
    private File script;
    private final String[] instructions = new String[1024];
    LinkedList<Integer> srcLineIndexes = new LinkedList<>();
    int programCounter;
    Deque<FunctionField> callStack = new ArrayDeque<>();

    public static final Pattern commentPattern = Pattern.compile("//");
    public static final Pattern labelPattern = Pattern.compile("^label ");
    HashMap<String, Integer> labelAddresses = new HashMap<>();

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
            } catch (SyntaxException e) {
                PlayerActionUtil.sendClientMessage(Component.translatable("message.i_am_robot.vm.syntax_error").withStyle(ChatFormatting.RED));
                PlayerActionUtil.sendClientMessage(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED));
                return;
            }

            callStack.push(new FunctionField(0, 0));
            try {
                while (AssemblerParser.evaluate(instructions[programCounter])) {
                    programCounter++;
                }
                PlayerActionUtil.sendClientMessage(Component.translatable("message.i_am_robot.vm.halt").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            } catch (Exception e) {
                PlayerActionUtil.sendClientMessage(Component.translatable("message.i_am_robot.vm.exception").withStyle(ChatFormatting.RED));
                String message = e.getClass().getSimpleName() + ": " + e.getMessage();
                PlayerActionUtil.sendClientMessage(Component.literal(message).withStyle(ChatFormatting.RED));

                int errLine = programCounter;
                while (callStack.size() > 1) {
                    FunctionField field = callStack.pop();
                    String stackTrace = String.format("at %s (line %d)", findLabel(field.address), srcLineIndexes.get(errLine));
                    PlayerActionUtil.sendClientMessage(Component.literal(stackTrace).withStyle(ChatFormatting.RED));
                    errLine = field.returnAddress;
                }
                String stackTrace = String.format("at <main> (line %d)", srcLineIndexes.get(errLine));
                PlayerActionUtil.sendClientMessage(Component.literal(stackTrace).withStyle(ChatFormatting.RED));
            }
        } finally {
            callStack.clear();
            labelAddresses.clear();
            srcLineIndexes.clear();
            running = false;
        }
    }

    private void readScript() throws FileNotFoundException, SyntaxException {
        try (Scanner sc = new Scanner(script)) {
            int instIndex = 0;
            int srcIndex = 0;
            while (sc.hasNextLine()) {
                String s = sc.nextLine();
                srcIndex++;

                Matcher commentMatcher = commentPattern.matcher(s);
                if (commentMatcher.find()) {
                    s = s.substring(0, commentMatcher.start());
                }

                if (s.isBlank()) continue;
                s = s.trim();

                Matcher labelMatcher = labelPattern.matcher(s);
                if (labelMatcher.find()) {
                    String labelName = s.substring(labelMatcher.end());
                    if (labelAddresses.put(labelName, instIndex) != null) throw new SyntaxException("Duplicate label name: " + labelName);
                } else {
                    srcLineIndexes.add(srcIndex);
                    instructions[instIndex] = s;
                    instIndex++;
                }
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

    public int getAddressForLabel(String label) {
        Integer i = labelAddresses.get(label);
        if (i == null) throw new IllegalArgumentException(String.format("No matching label for \"%s\"", label));
        return i;
    }

    public String findLabel(int address) {
        for (Map.Entry<String, Integer> entry : labelAddresses.entrySet()) {
            if (entry.getValue().equals(address)) {
                return entry.getKey();
            }
        }
        throw new NoSuchElementException("No label found with address " + address);
    }

    /**
     * 跳转到指定的位置
     * @param n 跳转后执行的下一条指令
     */
    private void jump(int n) {
        programCounter = n - 1;
    }

    /**
     * 跳转到指定的标签
     * @param label 目的标签
     * @throws IllegalArgumentException 若指定的标签不存在
     */
    public void jumpToLabel(String label) {
        jump(getAddressForLabel(label));
    }

    @SuppressWarnings("DataFlowIssue")
    public void createVariable(String name, Object value) {
        callStack.peek().variableTable.put(name, value);
    }

    @SuppressWarnings("DataFlowIssue")
    public Object getVariable(String name) {
        Object value = callStack.peek().variableTable.get(name);
        if (value == null) throw new VMRuntimeException(String.format("Variable \"%s\" is not defined", name));
        return value;
    }

    @SuppressWarnings("DataFlowIssue")
    public void setVariable(String name, Object newValue) {
        Object o = callStack.peek().variableTable.replace(name, newValue);
        if (o == null) throw new VMRuntimeException(String.format("Variable \"%s\" is not defined", name));
    }
}
