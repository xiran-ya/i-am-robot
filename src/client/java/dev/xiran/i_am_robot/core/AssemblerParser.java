package dev.xiran.i_am_robot.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一个脚本解释器，使用简易的类汇编语言
 */
public class AssemblerParser {
    public static final Pattern stringPattern = Pattern.compile("\".*\"");

    public static boolean evaluate(String instruction) throws SyntaxException {
        if (instruction == null) {
            PlayerActionUtil.sendClientMessage(Component.translatable("message.i_am_robot.vm.null_instruction").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            return false;
        }

        String[] tokens = instruction.trim().split(" +");
        try {
            switch (tokens[0]) {
                case "int" -> {
                    VirtualMachine.INSTANCE.createVariable(tokens[1], Integer.parseInt(tokens[2]));
                    return true;
                }
                case "double" -> {
                    VirtualMachine.INSTANCE.createVariable(tokens[1], Double.parseDouble(tokens[2]));
                    return true;
                }
                case "string", "String" -> {
                    Matcher matcher = stringPattern.matcher(instruction);
                    if (!matcher.find()) throw new SyntaxException("Missing argument, string expected");
                    VirtualMachine.INSTANCE.createVariable(tokens[1], instruction.substring(matcher.start() + 1, matcher.end() - 1));
                    return true;
                }
                case "mov" -> {
                    Object sourceObj;
                    if (tokens[1].charAt(0) == '"') {
                        Matcher matcher = stringPattern.matcher(instruction);
                        if (!matcher.find()) throw new SyntaxException("Missing argument, string expected");
                        sourceObj = instruction.substring(matcher.start() + 1, matcher.end() - 1);
                    } else {
                        sourceObj = parseValue(tokens[1]);
                    }

                    VirtualMachine.INSTANCE.setVariable(tokens[2], sourceObj);
                    return true;
                }
                case "add" -> {
                    Object var0 = VirtualMachine.INSTANCE.getVariable(tokens[1]);
                    Object var1 = parseValue(tokens[2]);

                    try {
                        if (var0 instanceof Integer) {
                            if (var1 instanceof Integer) {
                                VirtualMachine.INSTANCE.setVariable(tokens[3], (int) var0 + (int) var1);
                            } else {
                                VirtualMachine.INSTANCE.setVariable(tokens[3], (int) var0 + (double) var1);
                            }
                        } else {
                            if (var1 instanceof Integer) {
                                VirtualMachine.INSTANCE.setVariable(tokens[3], (double) var0 + (int) var1);
                            } else {
                                VirtualMachine.INSTANCE.setVariable(tokens[3], (double) var0 + (double) var1);
                            }
                        }
                        return true;
                    } catch (ClassCastException e) {
                        throw new VMRuntimeException("Cannot add with none-number variable");
                    }
                }
                case "format" -> {
                    Object format = VirtualMachine.INSTANCE.getVariable(tokens[1]);
                    if (format instanceof String) {
                        try {
                            Object[] formatArgs = new Object[tokens.length - 2];
                            for (int i = 0; i < tokens.length - 2; i++) {
                                formatArgs[i] = parseValue(tokens[1 + 2]);
                            }
                            String formattedString = String.format((String) format, formatArgs);
                            VirtualMachine.INSTANCE.setVariable(tokens[1], formattedString);
                            return true;
                        } catch (IllegalFormatException e) {
                            throw new VMRuntimeException("Illegal format string syntax");
                        }
                    } else {
                        throw new VMRuntimeException("Format can only be used on string variable");
                    }
                }
                case "log" -> {
                    PlayerActionUtil.sendClientMessage(Component.literal(parseValue(tokens[1]).toString()));
                    return true;
                }
                case "halt" -> {
                    PlayerActionUtil.sendClientMessage(Component.translatable("message.i_am_robot.vm.halt").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                    return false;
                }
                default -> throw new SyntaxException("Invalid keyword");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new SyntaxException("Instruction is missing argument");
        } catch (NumberFormatException e) {
            throw new SyntaxException("Invalid number format");
        }
    }

    /**
     * 解析字符串对应的值
     * @param string 输入字符串，可以是字面量或标识符
     * @return 字符串对应的值
     */
    private static Object parseValue(String string) {
        char c = string.charAt(0);
        if (Character.isDigit(c) || c == '+' || c == '-') {
            if (string.contains(".")) {
                return Double.parseDouble(string);
            } else {
                return Integer.parseInt(string);
            }
        } else {
            return VirtualMachine.INSTANCE.getVariable(string);
        }
    }
}
