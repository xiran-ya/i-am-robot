package dev.xiran.i_am_robot.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.IllegalFormatException;

/**
 * 一个脚本解释器，使用简易的类汇编语言
 */
public class AssemblerParser {
    public static boolean evaluate(String instruction) throws SyntaxException {
        if (instruction == null) {
            PlayerActionUtil.sendClientMessage(Component.translatable("message.i_am_robot.vm.null_instruction").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            return false;
        }

        String[] tokens = instruction.trim().split(" ");
        try {
            switch (tokens[0]) {
                case "int":
                    VirtualMachine.INSTANCE.createVariable(tokens[1], Integer.parseInt(tokens[2]));
                    return true;
                case "double":
                    VirtualMachine.INSTANCE.createVariable(tokens[1], Double.parseDouble(tokens[2]));
                    return true;
                case "string":
                    StringBuilder builder = new StringBuilder();
                    for (int i = 2; i < tokens.length; i++) {
                        builder.append(tokens[i]);
                        builder.append(' ');
                    }
                    if (!builder.isEmpty()) builder.deleteCharAt(builder.length() - 1);
                    VirtualMachine.INSTANCE.createVariable(tokens[1], builder.toString());
                    return true;
                case "add":
                    Object var0 = VirtualMachine.INSTANCE.getVariable(tokens[1]);
                    Object var1 = VirtualMachine.INSTANCE.getVariable(tokens[2]);
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
                case "format":
                    Object format = VirtualMachine.INSTANCE.getVariable(tokens[1]);
                    if (format instanceof String) {
                        try {
                            Object[] formatArgs = new Object[tokens.length - 2];
                            for (int i = 0; i < tokens.length - 2; i++) {
                                formatArgs[i] = VirtualMachine.INSTANCE.getVariable(tokens[i + 2]);
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
                case "log":
                    Object obj = VirtualMachine.INSTANCE.getVariable(tokens[1]);
                    PlayerActionUtil.sendClientMessage(Component.literal(obj.toString()));
                    return true;
                case "halt":
                    PlayerActionUtil.sendClientMessage(Component.translatable("message.i_am_robot.vm.halt").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                    return false;
                default:
                    throw new SyntaxException("Invalid keyword");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new SyntaxException("Instruction is missing argument");
        } catch (NumberFormatException e) {
            throw new SyntaxException("Invalid number format");
        }
    }
}
