package com.antkorwin.betterstrings.tokenizer;

import java.util.ArrayList;
import java.util.List;

import com.sun.tools.javac.tree.JCTree;

/**
 * Created on 2019-09-10
 * <p>
 * Split the string literal on a list of
 * literals and expressions.
 *
 * @author Korovin Anatoliy
 */
public class Tokenizer {

	private final String HEAD = "${";
	private final String TAIL = "}";

	public List<Token> split(JCTree.JCLiteral jcLiteral) {
		String literalValue = (String) jcLiteral.getValue();
		int originalOffset = jcLiteral.getPreferredPosition();
		return split(literalValue, originalOffset);
	}

	public List<Token> split(String literalValue) {
		return split(literalValue, 0);
	}

	public List<Token> split(String literalValue, int originalOffset) {
		List<Token> tokens = new ArrayList<>();
		int startIndex = 0;

		while (startIndex < literalValue.length()) {

			int headIndex = literalValue.indexOf(HEAD, startIndex);
			int offset = originalOffset + headIndex + HEAD.length() + 1;
			if (headIndex < 0) {
				tokens.add(new Token(literalValue.substring(startIndex), TokenType.STRING_LITERAL, offset));
				break;
			}

			int endIndex = literalValue.indexOf(TAIL, headIndex);
			if (endIndex < 0) {
				throw new RuntimeException("Not found ending bracket `}` of a variable declaration in string value: " +
				                           literalValue);
			}

			String prefix = literalValue.substring(startIndex, headIndex);
			if (!prefix.equals("")) {
				tokens.add(new Token(prefix, TokenType.STRING_LITERAL, offset));
			}

			String variable = literalValue.substring(headIndex + HEAD.length(), endIndex);
			if (!variable.equals("")) {
				tokens.add(new Token(variable, TokenType.EXPRESSION, offset));
			}

			startIndex = endIndex + TAIL.length();
		}

		return tokens;
	}
}
