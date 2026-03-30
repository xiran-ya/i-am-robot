package dev.xiran.i_am_robot.core;

public enum KeyWord {
    INT,
    DOUBLE,
    STRING,
    LOG;

    public static final String[] keyWordStrings = new String[] {
        "int", "double", "string", "log"
    };

    /**
     * 获取字符串对应的关键字
     * @param s 输入字符串
     * @return s 对应的关键字
     */
    public static KeyWord getKeyWord(String s) {
        return switch (s) {
            case "int" -> INT;
            case "double" -> DOUBLE;
            case "string" -> STRING;
            case "log" -> LOG;
            default -> throw new IllegalArgumentException("No matching keyword for given string");
        };
    }
}
