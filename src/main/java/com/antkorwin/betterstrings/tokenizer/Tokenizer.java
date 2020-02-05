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
		int offset = jcLiteral.getPreferredPosition();
		List<Token> tokens = split((String) jcLiteral.getValue());
		for (Token t : tokens) {
			t.setOffset(offset);
		}
		return tokens;
	}

	public List<Token> split(String literalValue) {

		List<Token> tokens = new ArrayList<>();
		int startIndex = 0;

		while (startIndex < literalValue.length()) {

			int headIndex = literalValue.indexOf(HEAD, startIndex);
			if (headIndex < 0) {
				tokens.add(new Token(literalValue.substring(startIndex), TokenType.STRING_LITERAL));
				break;
			}

			int endIndex = literalValue.indexOf(TAIL, headIndex);
			if (endIndex < 0) {
				throw new RuntimeException("Not found ending bracket `}` of a variable declaration in string value: " +
				                           literalValue);
			}

			String prefix = literalValue.substring(startIndex, headIndex);
			if (!prefix.equals("")) {
				tokens.add(new Token(prefix, TokenType.STRING_LITERAL));
			}

			String variable = literalValue.substring(headIndex + HEAD.length(), endIndex);
			if (!variable.equals("")) {
				tokens.add(new Token(variable, TokenType.EXPRESSION));
			}

			startIndex = endIndex + TAIL.length();
		}

		return tokens;
	}
}
