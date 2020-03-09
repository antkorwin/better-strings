package com.antkorwin.betterstrings;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.antkorwin.betterstrings.ast.BackslashEscapingAstTranslator;
import com.antkorwin.betterstrings.ast.InnerStringVarsAstTranslator;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

/**
 * Created on 2019-09-02
 * <p>
 * String interpolation annotation processor
 *
 * @author Korovin Anatoliy
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BetterStringsProcessor extends AbstractProcessor {

	private JavacProcessingEnvironment env;
	private Messager messager;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		messager = processingEnv.getMessager();
		env = (JavacProcessingEnvironment) processingEnv;
		super.init(processingEnv);
		printBanner();
	}


	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		if (roundEnv.processingOver()) {
			return false;
		}

		Context context = env.getContext();
		Trees trees = Trees.instance(env);

		for (Element codeElement : roundEnv.getRootElements()) {

			if (codeElement.getKind() != ElementKind.CLASS) {
				//TODO: check working with enums
				continue;
			}
			JCTree tree = (JCTree) trees.getPath(codeElement).getCompilationUnit();
			new BackslashEscapingAstTranslator(context).translate(tree);
			new InnerStringVarsAstTranslator(context).translate(tree);
		}

		return false;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	private void printBanner() {
		String banner = "\n" +
		                "___  ____ ___ ___ ____ ____    ____ ___ ____ _ _  _ ____ ____ \n" +
		                "|__] |___  |   |  |___ |__/    [__   |  |__/ | |\\ | | __ [__  \n" +
		                "|__] |___  |   |  |___ |  \\    ___]  |  |  \\ | | \\| |__] ___] \n" +
		                "   v0.1 String Interpolation Java Plugin, by Anatoliy Korovin  ";

		messager.printMessage(Diagnostic.Kind.NOTE, banner);
	}
}
