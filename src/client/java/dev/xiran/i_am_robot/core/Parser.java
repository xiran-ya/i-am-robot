package dev.xiran.i_am_robot.core;

import java.util.LinkedList;

public class Parser {
    /**
     * 解析并执行一条指令
     * @param instruction 要解析的指令
     * @return 如果这条指令不是 halt，则返回 true，否则返回 false
     */
    static boolean evaluate(String instruction) {
        return true;
    }

    static Expression parse(LinkedList<Token> tokens) throws SyntaxException {
        Token token0 = tokens.poll();
        if (token0 != null) {
            switch (token0.getType()) {
                case Token.Type.KEY_WORD:
                    KeyWord keyWord = token0.getKeyWord();
                    switch (keyWord) {
                        case KeyWord.INT:
                            Expression expr = new Expression(OperationType.INT);

                            break;
                        case KeyWord.DOUBLE:
                            break;
                        case KeyWord.STRING:
                            break;
                        case KeyWord.LOG:
                            break;
                    }
                    break;
                case Token.Type.IDENTIFIER:
                    break;
                default:
                    throw new SyntaxException("Invalid token at index 0");
            }
        }
        return null;
    }

    /**
     * 将指令转化为 token 序列
     * @param instruction 指令
     * @return Token 的序列（链表）
     * @throws SyntaxException 若解析时出现语法错误
     */
    static LinkedList<Token> tokenize(String instruction) throws SyntaxException {
        LinkedList<Token> tokens = new LinkedList<>();
        String[] words = instruction.trim().split(" ");

        // 检查第 0 个 word，判断是关键字还是标识符
        String word0 = words[0];
        boolean matchedKeyWord = false;
        for (String keyWord : KeyWord.keyWordStrings) {
            if (keyWord.equals(word0)) {
                Token keyWordToken = new Token(Token.Type.KEY_WORD, KeyWord.getKeyWord(keyWord));
                tokens.addLast(keyWordToken);
                matchedKeyWord = true;
                break;
            }
        }
        if (!matchedKeyWord) {
            // 未匹配任何关键字，则认为是标识符
            if (word0.matches("[a-zA-Z_$]\\w*")) {
                Token token = new Token(Token.Type.IDENTIFIER, word0);
                tokens.addLast(token);
            } else {
                throw new SyntaxException("Invalid token, expected keyword or identifier");
            }
        }

        // 解析剩下的 word
        for (int i = 1; i < words.length; i++) {
            char c = words[i].charAt(0);
            if (words[i].equals("+") || words[i].equals("-") || words[i].equals("*") || words[i].equals("/") || words[i].equals("%")) {
                Token token = new Token(Token.Type.OPERATOR, words[i]);
                tokens.addLast(token);
            } else if (c == '+' || c == '-' || Character.isDigit(c)) {
                try {
                    if (words[i].contains(".")) {
                        double value = Double.parseDouble(words[i]);
                        Token token = new Token(Token.Type.DOUBLE, value);
                        tokens.addLast(token);
                    } else {
                        int value = Integer.parseInt(words[i]);
                        Token token = new Token(Token.Type.INT, value);
                        tokens.addLast(token);
                    }
                } catch (NumberFormatException e) {
                    throw new SyntaxException("Invalid number format", e);
                }
            } else if (words[i].matches("[a-zA-Z_$]\\w*")) {
                Token token = new Token(Token.Type.IDENTIFIER, words[i]);
                tokens.addLast(token);
            } else {
                throw new SyntaxException(String.format("Invalid token \"%s\" at index %d", words[i], i));
            }
        }
        return tokens;
    }
}
