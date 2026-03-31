package dev.xiran.i_am_robot.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * 一个脚本解释器，使用简易的类汇编语言
 */
public class AssemblerParser {
    public static boolean evaluate(String instruction) throws SyntaxException {
        String[] tokens = instruction.trim().split(" ");
        try {
            switch (tokens[0]) {
                case "int":
                    VirtualMachine.INSTANCE.createVariable(tokens[1], Integer.parseInt(tokens[2]));
                    return true;
                case "double":
                    VirtualMachine.INSTANCE.createVariable(tokens[1], Double.parseDouble(tokens[2]));
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
                case "log":
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player != null) {
                        Object obj = VirtualMachine.INSTANCE.getVariable(tokens[1]);
                        player.displayClientMessage(Component.literal(obj.toString()), false);
                    }
                    return true;
                case "halt":
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
