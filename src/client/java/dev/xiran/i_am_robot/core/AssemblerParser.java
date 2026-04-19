package dev.xiran.i_am_robot.core;

import dev.xiran.i_am_robot.player.ContainerUtil;
import dev.xiran.i_am_robot.player.PlayerActionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.IllegalFormatException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一个脚本解释器，使用简易的类汇编语言
 */
public class AssemblerParser {
    public static final Pattern stringPattern = Pattern.compile("\".*\"");

    @SuppressWarnings("RedundantLabeledSwitchRuleCodeBlock")
    public static boolean evaluate(String instruction) throws SyntaxException, InterruptedException {
        if (instruction == null) {
            PlayerActionUtil.sendClientMessage(Component.translatable("message.i_am_robot.vm.null_instruction").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
            return false;
        }

        String[] tokens = instruction.split(" +");
        try {
            switch (tokens[0]) {
                case "int" -> {
                    VirtualMachine.INSTANCE.createVariable(tokens[1], tokens.length == 2 ? 0 : Integer.parseInt(tokens[2]));
                }
                case "double" -> {
                    VirtualMachine.INSTANCE.createVariable(tokens[1], tokens.length == 2 ? 0.0 : Double.parseDouble(tokens[2]));
                }
                case "string", "String" -> {
                    VirtualMachine.INSTANCE.createVariable(tokens[1], tokens.length == 2 ? "" : findString(instruction));
                }
                case "mov" -> {
                    Object sourceObj;
                    if (tokens[1].charAt(0) == '"') {
                        sourceObj = findString(instruction);
                    } else {
                        sourceObj = parseValue(tokens[1]);
                    }

                    VirtualMachine.INSTANCE.setVariable(tokens[tokens.length - 1], sourceObj);
                }
                case "call" -> {
                    Object[] args = new Object[tokens.length - 3];
                    for (int i = 0; i < tokens.length - 3; i++) {
                        args[i] = parseValue(tokens[i + 3]);
                    }
                    FunctionField functionField = new FunctionField(VirtualMachine.INSTANCE.programCounter, VirtualMachine.INSTANCE.getAddressForLabel(tokens[1]), tokens[2].charAt(0) == '-' ? null : tokens[2], args);

                    VirtualMachine.INSTANCE.callStack.push(functionField);
                    VirtualMachine.INSTANCE.jumpToLabel(tokens[1]);
                }
                case "return" -> {
                    if (VirtualMachine.INSTANCE.callStack.size() == 1) return false;
                    Object returnValue = null;
                    if (tokens.length > 1) returnValue = parseValue(tokens[1]);

                    FunctionField functionField = VirtualMachine.INSTANCE.callStack.pop();
                    VirtualMachine.INSTANCE.programCounter = functionField.returnAddress;

                    if (functionField.returnValueTo != null) {
                        if (returnValue == null) throw new VMRuntimeException("Function did not return desired value");
                        VirtualMachine.INSTANCE.setVariable(functionField.returnValueTo, returnValue);
                    }
                }
                case "+" -> {
                    Object var0 = VirtualMachine.INSTANCE.getVariable(tokens[1]);
                    Object var1 = parseValue(tokens[2]);
                    Object result = dualCalculate(var0, var1,
                        Integer::sum,
                        Double::sum,
                        Double::sum,
                        Double::sum
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[3], result);
                }
                case "-" -> {
                    Object var0 = parseValue(tokens[1]);
                    Object var1 = parseValue(tokens[2]);
                    Object result = dualCalculate(var0, var1,
                        (v0, v1) -> v0 - v1,
                        (v0, v1) -> v0 - v1,
                        (v0, v1) -> v0 - v1,
                        (v0, v1) -> v0 - v1
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[3], result);
                }
                case "*" -> {
                    Object var0 = parseValue(tokens[1]);
                    Object var1 = parseValue(tokens[2]);
                    Object result = dualCalculate(var0, var1,
                        (v0, v1) -> v0 * v1,
                        (v0, v1) -> v0 * v1,
                        (v0, v1) -> v0 * v1,
                        (v0, v1) -> v0 * v1
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[3], result);
                }
                case "/" -> {
                    Object var0 = parseValue(tokens[1]);
                    Object var1 = parseValue(tokens[2]);
                    Object result = dualCalculate(var0, var1,
                        (v0, v1) -> v0 / v1,
                        (v0, v1) -> v0 / v1,
                        (v0, v1) -> v0 / v1,
                        (v0, v1) -> v0 / v1
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[3], result);
                }
                case "%" -> {
                    Object var0 = parseValue(tokens[1]);
                    Object var1 = parseValue(tokens[2]);
                    Object result = dualCalculate(var0, var1,
                        (v0, v1) -> v0 % v1,
                        (v0, v1) -> {throw new TypeException("Cannot perform modulus on non-int value");},
                        (v0, v1) -> {throw new TypeException("Cannot perform modulus on non-int value");},
                        (v0, v1) -> {throw new TypeException("Cannot perform modulus on non-int value");}
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[3], result);
                }
                case "&" -> {
                    Object var0 = parseValue(tokens[1]);
                    Object var1 = parseValue(tokens[2]);
                    Object result = dualCalculate(var0, var1,
                        (v0, v1) -> v0 & v1,
                        (v0, v1) -> {throw new TypeException("Cannot perform \"&\" on non-int value");},
                        (v0, v1) -> {throw new TypeException("Cannot perform \"&\" on non-int value");},
                        (v0, v1) -> {throw new TypeException("Cannot perform \"&\" on non-int value");}
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[3], result);
                }
                case "|" -> {
                    Object var0 = parseValue(tokens[1]);
                    Object var1 = parseValue(tokens[2]);
                    Object result = dualCalculate(var0, var1,
                        (v0, v1) -> v0 | v1,
                        (v0, v1) -> {throw new TypeException("Cannot perform \"|\" on non-int value");},
                        (v0, v1) -> {throw new TypeException("Cannot perform \"|\" on non-int value");},
                        (v0, v1) -> {throw new TypeException("Cannot perform \"|\" on non-int value");}
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[3], result);
                }
                case "!" -> {
                    Object value = parseValue(tokens[1]);
                    Object result = monoCalculate(value,
                        (v) -> v == 0 ? 1 : 0,
                        (v) -> {throw new TypeException("Cannot perform \"!\" on non-int value");}
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[2], result);
                }
                case "~" -> {
                    Object value = parseValue(tokens[1]);
                    Object result = monoCalculate(value,
                        (v) -> ~v,
                        (v) -> {throw new TypeException("Cannot perform \"~\" on non-int value");}
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[2], result);
                }
                case "^" -> {
                    Object var0 = parseValue(tokens[1]);
                    Object var1 = parseValue(tokens[2]);
                    Object result = dualCalculate(var0, var1,
                            (v0, v1) -> v0 ^ v1,
                            (v0, v1) -> {throw new TypeException("Cannot perform \"^\" on non-int value");},
                            (v0, v1) -> {throw new TypeException("Cannot perform \"^\" on non-int value");},
                            (v0, v1) -> {throw new TypeException("Cannot perform \"^\" on non-int value");}
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[3], result);
                }
                case "jumpIf" -> {
                    if (tokens.length == 3) {
                        Object condition = parseValue(tokens[1]);
                        if (condition instanceof Integer) {
                            if ((int) condition != 0) VirtualMachine.INSTANCE.jumpToLabel(tokens[2]);
                        } else {
                            throw new IllegalArgumentException("Cannot use non-int value as condition");
                        }
                    } else {
                        Object var0 = VirtualMachine.INSTANCE.getVariable(tokens[1]);
                        Object var1 = parseValue(tokens[3]);
                        String operation = tokens[2];
                        boolean result = switch (operation) {
                            case "==" -> {
                                yield (boolean) dualCalculate(var0, var1,
                                    Integer::equals,
                                    (v0, v1) -> false,
                                    (v0, v1) -> false,
                                    Double::equals
                                );
                            }
                            case "!=" -> {
                                yield (boolean) dualCalculate(var0, var1,
                                    (v0, v1) -> !v0.equals(v1),
                                    (v0, v1) -> true,
                                    (v0, v1) -> true,
                                    (v0, v1) -> !v0.equals(v1)
                                );
                            }
                            case ">" -> {
                                yield (boolean) dualCalculate(var0, var1,
                                    (v0, v1) -> v0 > v1,
                                    (v0, v1) -> v0 > v1,
                                    (v0, v1) -> v0 > v1,
                                    (v0, v1) -> v0 > v1
                                );
                            }
                            case ">=" -> {
                                yield (boolean) dualCalculate(var0, var1,
                                    (v0, v1) -> v0 >= v1,
                                    (v0, v1) -> v0 >= v1,
                                    (v0, v1) -> v0 >= v1,
                                    (v0, v1) -> v0 >= v1
                                );
                            }
                            case "<" -> {
                                yield (boolean) dualCalculate(var0, var1,
                                    (v0, v1) -> v0 < v1,
                                    (v0, v1) -> v0 < v1,
                                    (v0, v1) -> v0 < v1,
                                    (v0, v1) -> v0 < v1
                                );
                            }
                            case "<=" -> {
                                yield (boolean) dualCalculate(var0, var1,
                                    (v0, v1) -> v0 <= v1,
                                    (v0, v1) -> v0 <= v1,
                                    (v0, v1) -> v0 <= v1,
                                    (v0, v1) -> v0 <= v1
                                );
                            }
                            default -> throw new SyntaxException("Invalid compare operator: " + operation);
                        };
                        if (result) VirtualMachine.INSTANCE.jumpToLabel(tokens[4]);
                    }
                }
                case "jump" -> {
                    VirtualMachine.INSTANCE.jumpToLabel(tokens[1]);
                }
                case "sleep" -> {
                    if (parseValue(tokens[1]) instanceof Integer time) {
                        Thread.sleep(50L * time);
                    } else {
                        throw new TypeException("sleep only accept int argument");
                    }
                }
                case "attack" -> {
                    switch (tokens[1]) {
                        case "hold" -> PlayerActionUtil.setHoldAttack(true);
                        case "stop" -> PlayerActionUtil.setHoldAttack(false);
                        case "once" -> PlayerActionUtil.attack();
                        default -> throw new SyntaxException("Invalid argument for attack");
                    }
                }
                case "use" -> {
                    switch (tokens[1]) {
                        case "hold" -> PlayerActionUtil.setHoldUse(true);
                        case "stop" -> PlayerActionUtil.setHoldUse(false);
                        case "once" -> PlayerActionUtil.use();
                        default -> throw new SyntaxException("Invalid argument for use");
                    }
                }
                case "hotbar" -> {
                    if (parseValue(tokens[1]) instanceof Integer index) {
                        PlayerActionUtil.hotbar(index);
                    } else {
                        throw new TypeException("Int expected for hotbar index");
                    }
                }
                case "rot" -> {
                    if ((parseValue(tokens[1]) instanceof Double yaw) && (parseValue(tokens[2]) instanceof Double pitch)) {
                        PlayerActionUtil.rot(yaw, pitch);
                    } else {
                        throw new TypeException("Double expected for rot");
                    }
                }
                case "item" -> {
                    if (tokens[1].equals("get")) {
                        if (parseValue(tokens[2]) instanceof Integer count && parseValue(tokens[3]) instanceof Integer inventorySlot) {
                            ContainerUtil.getItem(count, inventorySlot);
                        } else {
                            throw new TypeException("Int expected for item count & slot");
                        }
                    } else {
                        if (parseValue(tokens[2]) instanceof Integer inventorySlot) {
                            ContainerUtil.putItem(inventorySlot);
                        } else {
                            throw new TypeException("Int expected for inventory slot");
                        }
                    }
                }
                case "pow" -> {
                    Object var0 = parseValue(tokens[1]);
                    Object var1 = parseValue(tokens[2]);
                    Object result = dualCalculate(var0, var1,
                        Math::pow,
                        Math::pow,
                        Math::pow,
                        Math::pow
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[3], result);
                }
                case "sin" -> {
                    Object value = parseValue(tokens[1]);
                    Object result = monoCalculate(value,
                        Math::sin,
                        Math::sin
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[2], result);
                }
                case "cos" -> {
                    Object value = parseValue(tokens[1]);
                    Object result = monoCalculate(value,
                        Math::cos,
                        Math::cos
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[2], result);
                }
                case "tan" -> {
                    Object value = parseValue(tokens[1]);
                    Object result = monoCalculate(value,
                        Math::tan,
                        Math::tan
                    );
                    VirtualMachine.INSTANCE.setVariable(tokens[2], result);
                }
                case "format" -> {
                    Object format = VirtualMachine.INSTANCE.getVariable(tokens[1]);
                    if (format instanceof String) {
                        try {
                            Object[] formatArgs = new Object[tokens.length - 2];
                            for (int i = 0; i < tokens.length - 2; i++) {
                                formatArgs[i] = parseValue(tokens[i + 2]);
                            }
                            String formattedString = String.format((String) format, formatArgs);
                            VirtualMachine.INSTANCE.setVariable(tokens[1], formattedString);
                        } catch (IllegalFormatException e) {
                            throw new VMRuntimeException("Illegal format string syntax");
                        }
                    } else {
                        throw new TypeException("Format can only be used on string variable");
                    }
                }
                case "log" -> {
                    String message;
                    if (tokens[1].charAt(0) == '"') {
                        message = findString(instruction);
                    } else {
                        message = parseValue(tokens[1]).toString();
                    }
                    PlayerActionUtil.sendClientMessage(Component.literal(message));
                }
                case "halt" -> {
                    return false;
                }
                default -> throw new SyntaxException("Invalid keyword: " + tokens[0]);
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new SyntaxException("Instruction is missing argument");
        } catch (NumberFormatException e) {
            throw new SyntaxException("Invalid number format");
        }
    }

    private static Object monoCalculate(Object value, Function<Integer, Object> opInt, Function<Double, Object> opDouble) {
        if (value instanceof Integer intValue) {
            return opInt.apply(intValue);
        } else if (value instanceof Double doubleValue) {
            return opDouble.apply(doubleValue);
        } else {
            throw new TypeException("Cannot perform calculation with non-number variable");
        }
    }

    private static Object dualCalculate(Object arg0, Object arg1, DualOperation<Integer, Integer> opII, DualOperation<Integer, Double> opID, DualOperation<Double, Integer> opDI, DualOperation<Double, Double> opDD) {
        if (arg0 instanceof Integer int0) {
            if (arg1 instanceof Integer int1) {
                return opII.apply(int0, int1);
            } else if (arg1 instanceof Double double1) {
                return opID.apply(int0, double1);
            }
        } else if (arg0 instanceof Double double0) {
            if (arg1 instanceof Integer int1) {
                return opDI.apply(double0, int1);
            } else if (arg1 instanceof Double double1) {
                return opDD.apply(double0, double1);
            }
        }
        throw new TypeException("Cannot perform calculation with non-number variable");
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

    private static String findString(String instruction) throws SyntaxException {
        Matcher matcher = stringPattern.matcher(instruction);
        if (!matcher.find()) throw new SyntaxException("Missing argument, string expected");
        return instruction.substring(matcher.start() + 1, matcher.end() - 1);
    }
}
