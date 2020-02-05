package com.antkorwin.betterstrings.tokenizer;


import java.util.List;

import com.antkorwin.betterstrings.tokenizer.Token;
import com.antkorwin.betterstrings.tokenizer.Tokenizer;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.antkorwin.betterstrings.tokenizer.TokenType.STRING_LITERAL;
import static com.antkorwin.betterstrings.tokenizer.TokenType.EXPRESSION;
import static org.assertj.core.api.Assertions.assertThat;

class TokenizerTest {

    @Test
    void splitComplexLiteral() {
        // Arrange
        List<Token> tokens = new Tokenizer().split("expr : ${a}+${b}=${sum}!");
        // Assert
        assertThat(tokens).extracting(Token::getValue, Token::getType)
                          .containsExactly(Tuple.tuple("expr : ", STRING_LITERAL),
                                           Tuple.tuple("a", EXPRESSION),
                                           Tuple.tuple("+", STRING_LITERAL),
                                           Tuple.tuple("b", EXPRESSION),
                                           Tuple.tuple("=", STRING_LITERAL),
                                           Tuple.tuple("sum", EXPRESSION),
                                           Tuple.tuple("!", STRING_LITERAL));
    }

    @Test
    void splitSingleVariable() {
        // Arrange
        List<Token> tokens = new Tokenizer().split("${variable}");
        // Assert
        assertThat(tokens).extracting(Token::getValue, Token::getType)
                          .containsExactly(Tuple.tuple("variable", EXPRESSION));
    }

    @Test
    void splitConcatenation() {
        // Arrange
        List<Token> tokens = new Tokenizer().split("concat=${a}${b}${c}");
        // Assert
        assertThat(tokens).extracting(Token::getValue, Token::getType)
                          .containsExactly(Tuple.tuple("concat=", STRING_LITERAL),
                                           Tuple.tuple("a", EXPRESSION),
                                           Tuple.tuple("b", EXPRESSION),
                                           Tuple.tuple("c", EXPRESSION));
    }

    @Test
    void splitVariableInTheMiddleOfString() {
        // Arrange
        List<Token> tokens = new Tokenizer().split("prefix${val}suffix");
        // Assert
        assertThat(tokens).extracting(Token::getValue, Token::getType)
                          .containsExactly(Tuple.tuple("prefix", STRING_LITERAL),
                                           Tuple.tuple("val", EXPRESSION),
                                           Tuple.tuple("suffix", STRING_LITERAL));
    }

    @Test
    void twoVars() {
        // Arrange
        List<Token> tokens = new Tokenizer().split("${firstVar}${secondVar}");
        // Assert
        assertThat(tokens).extracting(Token::getValue, Token::getType)
                          .containsExactly(Tuple.tuple("firstVar", EXPRESSION),
                                           Tuple.tuple("secondVar", EXPRESSION));
    }

    @Test
    void twoVarsSplitBySingleChar() {
        // Arrange
        List<Token> tokens = new Tokenizer().split("${firstVar}=${secondVar}");
        // Assert
        assertThat(tokens).extracting(Token::getValue, Token::getType)
                          .containsExactly(Tuple.tuple("firstVar", EXPRESSION),
                                           Tuple.tuple("=", STRING_LITERAL),
                                           Tuple.tuple("secondVar", EXPRESSION));
    }

    @Test
    void twoVarsSplitByMultipleChars() {
        // Arrange
        List<Token> tokens = new Tokenizer().split("${firstVar} some \t\ndata ${secondVar}");
        // Assert
        assertThat(tokens).extracting(Token::getValue, Token::getType)
                          .containsExactly(Tuple.tuple("firstVar", EXPRESSION),
                                           Tuple.tuple(" some \t\ndata ", STRING_LITERAL),
                                           Tuple.tuple("secondVar", EXPRESSION));
    }

    @Test
    void wrongValueWithoutTail() {

        RuntimeException exception =
                Assertions.assertThrows(RuntimeException.class,
                                        () -> new Tokenizer().split("${aaa"));
        assertThat(exception.getMessage()).isEqualTo("Not found ending bracket `}` of a variable declaration in string value: ${aaa");
    }

    @Test
    void withoutVariables() {
        // Arrange
        List<Token> tokens = new Tokenizer().split("just a text");
        // Assert
        assertThat(tokens).extracting(Token::getValue, Token::getType)
                          .containsExactly(Tuple.tuple("just a text", STRING_LITERAL));
    }
}