package com.antkorwin.betterstrings.tokenizer;

/**
 * Created on 2019-09-11
 *
 * internal type which used to split string
 * literal with expressions on the list of tokens.
 *
 * @author Korovin Anatoliy
 */
public class Token {

    private final String value;
    private final TokenType type;
    private final int offset;

    public Token(String value, TokenType type, int offset) {
        this.value = value;
        this.type = type;
        this.offset = offset;
    }

    public String getValue() {
        return value;
    }

    public TokenType getType() {
        return type;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "Token{" +
               "value='" + value + '\'' +
               ", type=" + type +
               '}';
    }
}
