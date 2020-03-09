package com.antkorwin.betterstrings.ast;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;

public class BackslashEscapingAstTranslator extends TreeTranslator {

	private final TreeMaker treeMaker;


	public BackslashEscapingAstTranslator(Context context) {
		this.treeMaker = TreeMaker.instance(context);
	}

	@Override
	public <T extends JCTree> T translate(T t) {
		return super.translate(t);
	}

	@Override
	public void visitLiteral(JCTree.JCLiteral jcLiteral) {

		super.visitLiteral(jcLiteral);

		if (jcLiteral.getValue() instanceof String) {
			String source = (String) jcLiteral.getValue();
			if (source.contains("\\$")) {
				String escaped = escapeInterpolation(source);
				result = treeMaker.Literal(escaped);
			}
		}
	}

	private String escapeInterpolation(String value) {
		return value.replaceAll("\\$","${'$'}");
	}
}
