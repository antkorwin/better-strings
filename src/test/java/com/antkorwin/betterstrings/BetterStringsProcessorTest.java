package com.antkorwin.betterstrings;

import com.jupitertools.compiletest.CompileTest;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BetterStringsProcessorTest {

	@Test
	void compileTest() {

		@Language("Java") String classCode = "public class Test { " +
		                                     "  public static String hello(){ " +
		                                     "      String x = \"Ho!\"; " +
		                                     "      return \"Hey-${x}\";" +
		                                     "  }" +
		                                     "}";

		Object result = new CompileTest().classCode("Test", classCode)
		                                 .processor(new BetterStringsProcessor())
		                                 .compile()
		                                 .loadClass("Test")
		                                 .invokeStatic("hello");

		assertThat(result).isEqualTo("Hey-Ho!");
	}
}