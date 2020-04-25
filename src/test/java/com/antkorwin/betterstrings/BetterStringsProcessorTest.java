package com.antkorwin.betterstrings;

import com.jupitertools.compiletest.CompileTest;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BetterStringsProcessorTest {

	@Test
	void simple() {

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

	@Test
	void evaluateExpression() {

		@Language("Java") String classCode = "public class Test { " +
		                                     "  public static String sum(){ " +
		                                     "      int x = 3;" +
		                                     "      int y = 4;" +
		                                     "      return \"${x} + ${y} = ${x+y}\";" +
		                                     "  }" +
		                                     "}";

		Object result = new CompileTest().classCode("Test", classCode)
		                                 .processor(new BetterStringsProcessor())
		                                 .compile()
		                                 .loadClass("Test")
		                                 .invokeStatic("sum");

		assertThat(result).isEqualTo("3 + 4 = 7");
	}

	@Test
	void createNewClassInExpression() {
		@Language("Java") String innerClass = "public class Inner {" +
		                                      "     public String password(){" +
		                                      "         return \"1234\";" +
		                                      "     }" +
		                                      "}";
		@Language("Java") String classCode = "public class Test { " +
		                                     "  public static String test(){ " +
		                                     "      int x = 3;" +
		                                     "      int y = 4;" +
		                                     "      return \"password = ${new Inner().password()}\";" +
		                                     "  }" +
		                                     "}";

		Object result = new CompileTest().classCode("Test", classCode)
		                                 .classCode("Inner", innerClass)
		                                 .processor(new BetterStringsProcessor())
		                                 .compile()
		                                 .loadClass("Test")
		                                 .invokeStatic("test");

		assertThat(result).isEqualTo("password = 1234");
	}

	@Nested
	class DisabledAnnotationTests {

		@Test
		void onField() {
			@Language("Java") String classCode = "public class Test { " +
			                                     "  @com.antkorwin.betterstrings.DisabledStringInterpolation" +
			                                     "  public String field = \"${3+4}\";" +
			                                     "  public String getField(){ " +
			                                     "      return \"${field}\";" +
			                                     "  }" +
			                                     "}";

			Object result = new CompileTest().classCode("Test", classCode)
			                                 .processor(new BetterStringsProcessor())
			                                 .compile()
			                                 .createClass("Test")
			                                 .invoke("getField");

			assertThat(result).isEqualTo("${3+4}");
		}

		@Test
		void onMethod() {
			@Language("Java") String classCode = "public class Test { " +
			                                     "  @com.antkorwin.betterstrings.DisabledStringInterpolation" +
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

			assertThat(result).isEqualTo("Hey-${x}");
		}

		@Test
		void onClass() {
			@Language("Java") String classCode = "@com.antkorwin.betterstrings.DisabledStringInterpolation " +
			                                     "public class Test { " +
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

			assertThat(result).isEqualTo("Hey-${x}");
		}

		@Test
		void onNestedClass() {
			@Language("Java") String classCode = "public class Test { " +
			                                     "  public static String sum(){ " +
			                                     "      return \"sum = ${new NestedClass().test()}\";" +
			                                     "  } " +
			                                     "  @com.antkorwin.betterstrings.DisabledStringInterpolation " +
			                                     "  public static class NestedClass {" +
			                                     "      public String test(){" +
			                                     "          return \"${3+4}\";" +
			                                     "      }" +
			                                     "  }" +
			                                     "}";

			Object result = new CompileTest().classCode("Test", classCode)
			                                 .processor(new BetterStringsProcessor())
			                                 .compile()
			                                 .loadClass("Test")
			                                 .invokeStatic("sum");

			assertThat(result).isEqualTo("sum = ${3+4}");
		}

		@Test
		void importDisabledAnnotationAndCheckType() {
			@Language("Java") String classCode = "import com.antkorwin.betterstrings.*;" +
			                                     "" +
			                                     "public class Test { " +
			                                     "  public static String sum(){ " +
			                                     "      return \"sum = ${new NestedClass().test()}\";" +
			                                     "  } " +
			                                     "  @DisabledStringInterpolation" +
			                                     "  public static class NestedClass {" +
			                                     "      public String test(){" +
			                                     "          return \"${3+4}\";" +
			                                     "      }" +
			                                     "  }" +
			                                     "}";

			Object result = new CompileTest().classCode("Test", classCode)
			                                 .processor(new BetterStringsProcessor())
			                                 .compile()
			                                 .loadClass("Test")
			                                 .invokeStatic("sum");

			assertThat(result).isEqualTo("sum = ${3+4}");
		}
	}

	@Nested
	class StringInterpolationEscaping {

		@Test
		void workaround() {
			@Language("Java") String classCode = "import com.antkorwin.betterstrings.*;" +
			                                     "" +
			                                     "public class Test { " +
			                                     "  public static String sum(){ " +
			                                     "      return \"sum = ${'$'}{3 + 4}\";" +
			                                     "  } " +
			                                     "}";

			Object result = new CompileTest().classCode("Test", classCode)
			                                 .processor(new BetterStringsProcessor())
			                                 .compile()
			                                 .loadClass("Test")
			                                 .invokeStatic("sum");

			assertThat(result).isEqualTo("sum = ${3 + 4}");
		}
	}

	@Nested
	class Enums {

		@Test
		void useExpression() {
			@Language("Java") String enumCode = "public enum EnumCode {" +
			                                    "   FIRST," +
			                                    "	SECOND;" +
			                                    "	String getVal() {" +
			                                    "		return \"enum = ${3 + 4}\";" +
			                                    "	}" +
			                                    "}";
			@Language("Java") String classCode = "public class Test { " +
			                                     "  public static String test(){ " +
			                                     "      return EnumCode.FIRST.getVal();" +
			                                     "  }" +
			                                     "}";

			Object result = new CompileTest().classCode("Test", classCode)
			                                 .classCode("EnumCode", enumCode)
			                                 .processor(new BetterStringsProcessor())
			                                 .compile()
			                                 .loadClass("Test")
			                                 .invokeStatic("test");

			assertThat(result).isEqualTo("enum = 7");
		}

		@Test
		void useEnumValue() {
			@Language("Java") String enumCode = "public enum EnumCode {" +
			                                    "   FIRST," +
			                                    "	SECOND;" +
			                                    "	String getVal() {" +
			                                    "		return \"${this.name()} = ${3 + 4}\";" +
			                                    "	}" +
			                                    "}";
			@Language("Java") String classCode = "public class Test { " +
			                                     "  public static String test(){ " +
			                                     "      return EnumCode.SECOND.getVal();" +
			                                     "  }" +
			                                     "}";

			Object result = new CompileTest().classCode("Test", classCode)
			                                 .classCode("EnumCode", enumCode)
			                                 .processor(new BetterStringsProcessor())
			                                 .compile()
			                                 .loadClass("Test")
			                                 .invokeStatic("test");

			assertThat(result).isEqualTo("SECOND = 7");
		}
	}
}