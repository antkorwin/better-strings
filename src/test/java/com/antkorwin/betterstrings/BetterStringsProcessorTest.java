package com.antkorwin.betterstrings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class BetterStringsProcessorTest {

	@Test
	void name() throws Exception {

		String program =
				"public class Test { " +
				"public static String hello(){ " +
				"String x = \"Ho!\"; " +
				"return \"Hey-${x}\";" +
				"}" +
				" }";

		ClassLoader classLoader = getCompiledClassLoader("Test", program);
		Class<?> testClass = classLoader.loadClass("Test");
		Method method = testClass.getMethod("hello", null);
		String result = (String) method.invoke(null);

		assertThat(result).isEqualTo("Hey-Ho!");
	}

	private ClassLoader getCompiledClassLoader(String className, String program) {

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		JavaFileObject compilationUnit = new StringJavaFileObject(className, program);

		SimpleJavaFileManager fileManager =
				new SimpleJavaFileManager(compiler.getStandardFileManager(null,
				                                                          null,
				                                                          null));

		JavaCompiler.CompilationTask compilationTask =
				compiler.getTask(null,
				                 fileManager,
				                 null,
				                 null,
				                 null,
				                 Arrays.asList(compilationUnit));

		compilationTask.setProcessors(Arrays.asList(new BetterStringsProcessor()));
		compilationTask.call();

		return new CompiledClassLoader(fileManager.getGeneratedOutputFiles());
	}

	private static class StringJavaFileObject extends SimpleJavaFileObject {
		private final String code;

		public StringJavaFileObject(String name, String code) {
			super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
			      Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return code;
		}
	}

	private static class CompiledClassLoader extends ClassLoader {
		private final List<ClassJavaFileObject> files;

		private CompiledClassLoader(List<ClassJavaFileObject> files) {
			this.files = files;
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			Iterator<ClassJavaFileObject> itr = files.iterator();
			while (itr.hasNext()) {
				ClassJavaFileObject file = itr.next();
				if (file.getClassName().equals(name)) {
					itr.remove();
					byte[] bytes = file.getBytes();
					return super.defineClass(name, bytes, 0, bytes.length);
				}
			}
			return super.findClass(name);
		}
	}

	private static class ClassJavaFileObject extends SimpleJavaFileObject {
		private final ByteArrayOutputStream outputStream;
		private final String className;

		protected ClassJavaFileObject(String className, Kind kind) {
			super(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind);
			this.className = className;
			outputStream = new ByteArrayOutputStream();
		}

		@Override
		public OutputStream openOutputStream() throws IOException {
			return outputStream;
		}

		public byte[] getBytes() {
			return outputStream.toByteArray();
		}

		public String getClassName() {
			return className;
		}
	}

	private static class SimpleJavaFileManager extends ForwardingJavaFileManager {
		private final List<ClassJavaFileObject> outputFiles;

		protected SimpleJavaFileManager(JavaFileManager fileManager) {
			super(fileManager);
			outputFiles = new ArrayList<>();
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location,
		                                           String className,
		                                           JavaFileObject.Kind kind,
		                                           FileObject sibling) throws IOException {
			ClassJavaFileObject file = new ClassJavaFileObject(className, kind);
			outputFiles.add(file);
			return file;
		}

		public List<ClassJavaFileObject> getGeneratedOutputFiles() {
			return outputFiles;
		}
	}
}