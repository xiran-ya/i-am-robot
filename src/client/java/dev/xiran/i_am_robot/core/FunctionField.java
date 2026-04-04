package dev.xiran.i_am_robot.core;

import java.util.HashMap;

public class FunctionField {
    int returnAddress;
    String returnValueTo;
    HashMap<String, Object> variableTable;

    /**
     * 创建一个函数作用域
     * @param returnAddress 返回处的地址
     * @param returnValueTo 返回值要赋予的变量的名称，为 null 表示不使用返回值
     * @param args 函数的参数
     */
    public FunctionField(int returnAddress, String returnValueTo, Object[] args) {
        this.returnAddress = returnAddress;
        this.returnValueTo = returnValueTo;

        variableTable = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            variableTable.put("arg" + i, args[i]);
        }
    }

    /**
     * 创建一个函数作用域，不指定返回值和参数
     * @param returnAddress 返回处的地址
     */
    public FunctionField(int returnAddress) {
        this.returnAddress = returnAddress;
        variableTable = new HashMap<>();
    }
}
