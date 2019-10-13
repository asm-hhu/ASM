package com.hhu.coreapi.classvisitor;

import org.objectweb.asm.*;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import static org.objectweb.asm.Opcodes.ASM7;



public class ClassVisitorTool {

	private static PrintWriter printWriter;

	public ClassVisitorTool() {
		File file = new File("./TraceClassVisitor.txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
				printWriter = new PrintWriter(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void agentmain(String agentArgs, Instrumentation instrumentation) throws UnmodifiableClassException {
		System.out.println("agentmain call");
		instrumentation.addTransformer(new MyClassFileTransformer(), true);
		Class[] classes = instrumentation.getAllLoadedClasses();
		for (Class clazz : classes) {
			if ("com.hhu.coreapi.classvisitor.TargetClass".equals(clazz.getName())) {
				System.out.println("reloading " + clazz.getName());
				instrumentation.retransformClasses(clazz);
				break;
			}
		}
	}

	private static class MyClassFileTransformer implements ClassFileTransformer {
		@Override
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
			System.out.println(className);
			if (!"com/hhu/coreapi/classvisitor/TargetClass".equals(className)) {
				return classfileBuffer;
			}

			ClassReader cr = new ClassReader(classfileBuffer);
			ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			// ClassVisitor cv = new MyClassVisitor(ASM7, cw);

			// through TraceClassVisitor, we can print a textual representation of these call to PrintWriter
			TraceClassVisitor tcv = new TraceClassVisitor(cw, printWriter);

			// through CheckClassAdapter, we can check classes at this point in the chain,
			// if generate the invalid class that will be rejected by the JVM
			// it will detect the errors as soon as possible, and throw the Exception(IllegalStateException or IllegalArgumentException )
			CheckClassAdapter cca = new CheckClassAdapter(tcv);

			cr.accept(tcv, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			return cw.toByteArray();
		}
	}

}
