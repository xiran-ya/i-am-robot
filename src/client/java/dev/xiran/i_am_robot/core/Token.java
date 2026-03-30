package dev.xiran.i_am_robot.core;

/**
 * 语句中的一个 Token
 */
public class Token {
    Type type;
    Object value;

    public Token(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getString() {
        if (value instanceof String) {
            return (String) value;
        } else {
            throw new ClassCastException("Failed to cast to String");
        }
    }

    public KeyWord getKeyWord() {
        if (value instanceof KeyWord) {
            return (KeyWord) value;
        } else {
            throw new ClassCastException("Failed to cast to KeyWord");
        }
    }

    public int getInt() {
        if (value instanceof Integer) {
            return (int) value;
        } else {
            throw new ClassCastException("Failed to cast to int");
        }
    }

    public double getDouble() {
        if (value instanceof Double) {
            return (double) value;
        } else {
            throw new ClassCastException("Failed to cast to double");
        }
    }

    public enum Type {
        KEY_WORD,
        IDENTIFIER,
        INT,
        DOUBLE,
        OPERATOR;
    }
}
