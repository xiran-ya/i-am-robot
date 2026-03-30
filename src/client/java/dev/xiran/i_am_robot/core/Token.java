package dev.xiran.i_am_robot.core;

/**
 * 语句中的一个 Token
 * @param <C> Token 包含的内容的类型 (String, Integer 等)
 */
public class Token<C> {
    Type type;
    C value;

    public Token(Type type, C value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public C getValue() {
        return value;
    }

    public enum Type {
        KEY_WORD,
        IDENTIFIER,
        INT,
        DOUBLE,
        OPERATOR;
    }
}
