package com.hhu.coreapi.methodvisitor;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ASM7;

public class AdviceAdapterTool {

	private static String owner;

	public static void agentmain(String agentArgs, Instrumentation instrumentation) throws UnmodifiableClassException {
		System.out.println("agentmain call");
		instrumentation.addTransformer(new MyClassFileTransformer(), true);
		Class[] classes = instrumentation.getAllLoadedClasses();
		for (Class clazz : classes) {
			if ("com.hhu.attach.C".equals(clazz.getName())) {
				System.out.println("reloading " + clazz.getName());
				instrumentation.retransformClasses(clazz);
				break;
			}
		}
	}

	private static class MyClassFileTransformer implements ClassFileTransformer {
		@Override
		public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
			if (!"com/hhu/attach/C".equals(className)) {
				return classfileBuffer;
			}
			ClassReader cr = new ClassReader(classfileBuffer);
			ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
			ClassVisitor cv = new MyClassVisitor(ASM7, cw);
			cr.accept(cv, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			return cw.toByteArray();
		}
	}

	private static class MyClassVisitor extends ClassVisitor {
		MyClassVisitor(int api, ClassVisitor classVisitor) {
			super(api, classVisitor);
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			super.visit(version, access, name, signature, superName, interfaces);
			owner = name;
			System.out.println("owner: " + owner);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
			MethodVisitor mv =  super.visitMethod(access, name, descriptor, signature, exceptions);
			if (!"m".equals(name)) {
				return mv;
			}
			return new MyMethodVisitor(ASM7, mv, access, name, descriptor);
		}
	}

	/**
	 * public class C {
	 // *     // public static long timer;
	 *     public void m() throws Exception {
	 *        // timer -= System.currentTimeMillis();
	 *         Thread.sleep(100);
	 *         //timer += System.currentTimeMillis();
	 *         //System.out.println(timer);
	 *     }
	 * }
	 * add the // marked line source code
	 */
	private static class MyMethodVisitor extends AdviceAdapter {
		MyMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
			super(api, methodVisitor, access, name, descriptor);
		}

		@Override
		protected void onMethodEnter() {
			super.onMethodEnter();
			mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis",
					"()J", false);
			mv.visitInsn(LSUB);
			mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
		}

		@Override
		protected void onMethodExit(int opcode) {
			super.onMethodExit(opcode);
			mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis",
					"()J", false);
			mv.visitInsn(LADD);
			mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");

			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					"(J)V", false);
		}
	}


}
