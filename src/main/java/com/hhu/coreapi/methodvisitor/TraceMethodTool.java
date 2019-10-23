package com.hhu.coreapi.methodvisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

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

public class TraceMethodTool {

	private static PrintWriter printWriter;
	private static File file;

	static {
		file = new File("./TargetMethod.txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void agentmain(String agentArgs, Instrumentation instrumentation) throws UnmodifiableClassException {
		System.out.println("agentmain call...");
		instrumentation.addTransformer(new MyClassFileTransformer(), true);
		Class[] classes = instrumentation.getAllLoadedClasses();
		for (Class clazz : classes) {
			if ("com.hhu.attach.TargetClass".equals(clazz.getName())) {
				System.out.println("reload " + clazz.getName());
				instrumentation.retransformClasses(clazz);
			}
		}
	}

	protected static class MyClassFileTransformer implements ClassFileTransformer {
		@Override
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
			if ("com/hhu/coreapi/TargetClass".equals(className)) {
				ClassReader cr = new ClassReader(classfileBuffer);
				ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
				ClassVisitor cv = new MyClassVisitor(ASM7, cw);
				cr.accept(cv, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
				return cw.toByteArray();
			}
			return classfileBuffer;
		}
	}

	protected static class MyClassVisitor extends ClassVisitor {
		MyClassVisitor(int api, ClassVisitor classVisitor) {
			super(api, classVisitor);
		}

		/**
		 * like TraceClassVisitor
		 * print a textual representation of these call to PrintWriter
		 */
		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
			MethodVisitor mv =  super.visitMethod(access, name, descriptor, signature, exceptions);
			if ("getName".equals(name)) {
				System.out.println("getName");
				try {
					printWriter = new PrintWriter(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				Printer printer = new Textifier();
				printer.print(printWriter);
				TraceMethodVisitor tmv = new TraceMethodVisitor(mv, printer);
				return tmv;
			}
			return mv;
		}
	}


}
