package com.antkorwin.betterstrings.ast;

import java.util.List;
import java.util.function.Supplier;

import com.antkorwin.betterstrings.DisabledStringInterpolation;
import com.antkorwin.betterstrings.tokenizer.Token;
import com.antkorwin.betterstrings.tokenizer.Tokenizer;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

/**
 * Created on 2019-09-10
 * <p>
 * TODO: replace on the JavaDoc
 *
 * @author Korovin Anatoliy
 */
public class InnerStringVarsAstTranslator extends TreeTranslator {

	private final TreeMaker treeMaker;
	private final Tokenizer tokenizer;
	private final ExpressionParser expressionParser;

	private boolean skip;


	public InnerStringVarsAstTranslator(Context context) {
		this.treeMaker = TreeMaker.instance(context);
		this.tokenizer = new Tokenizer();
		this.expressionParser = new ExpressionParser(Names.instance(context));
	}

	@Override
	public <T extends JCTree> T translate(T t) {
		return super.translate(t);
	}

	@Override
	public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {

		doWithSkipResolving(() -> skip || isAnnotatedBySkip(jcClassDecl.getModifiers()),
		                    () -> super.visitClassDef(jcClassDecl));
	}

	@Override
	public void visitAnnotation(JCTree.JCAnnotation jcAnnotation) {

		doWithSkipResolving(() -> true,
		                    () -> super.visitAnnotation(jcAnnotation));
	}

	@Override
	public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {

		doWithSkipResolving(() -> skip || isAnnotatedBySkip(jcMethodDecl.getModifiers()),
		                    () -> super.visitMethodDef(jcMethodDecl));
	}

	@Override
	public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {

		doWithSkipResolving(() -> skip || isAnnotatedBySkip(jcVariableDecl.getModifiers()),
		                    () -> super.visitVarDef(jcVariableDecl));
	}

	private boolean isAnnotatedBySkip(JCTree.JCModifiers modifiers) {

		for (JCTree.JCAnnotation annotation : modifiers.getAnnotations()) {
			if (annotation.type.toString().equals(DisabledStringInterpolation.class.getCanonicalName())) {
				return true;
			}
		}
		return false;
	}

	private void doWithSkipResolving(Supplier<Boolean> skipCondition, Runnable run) {
		boolean skipBefore = skip;
		skip = skipCondition.get();
		run.run();
		skip = skipBefore;
	}


	@Override
	public void visitLiteral(JCTree.JCLiteral jcLiteral) {

		super.visitLiteral(jcLiteral);
		if (skip) {
			return;
		}

		if (jcLiteral.getValue() instanceof String) {

			List<Token> tokens = tokenizer.split(jcLiteral);

			if (tokens.size() < 1) {
				return;
			}

			if (tokens.size() == 1) {
				result = convertToExpression(tokens.get(0));
				return;
			}

			JCTree.JCExpression exprLeft = convertToExpression(tokens.get(0));
			for (int i = 1; i < tokens.size(); i++) {
				JCTree.JCExpression exprRight = convertToExpression(tokens.get(i));
				exprLeft = treeMaker.Binary(JCTree.Tag.PLUS, exprLeft, exprRight);
			}
			result = exprLeft;
		}
	}


	private JCTree.JCExpression convertToExpression(Token token) {
		switch (token.getType()) {
			case EXPRESSION:
				return expressionParser.parse(token);
			case STRING_LITERAL:
				return treeMaker.Literal(token.getValue());
			default:
				throw new RuntimeException("Unexpected token type: " + token.getType());
		}
	}
}
