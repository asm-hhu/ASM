package com.hhu.coreapi.classvisitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class RemoveFieldAndMethod extends ClassVisitor {

	private String fieldName;
	private String methodName;

	public RemoveFieldAndMethod(int api) {
		super(api);
	}

	/**
	 * delete Field by don't forward a method call
	 * if the visitXxx has an return value, just return null
	 */
	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		if (fieldName.equals(name)) {
			return null;
		}
		return super.visitField(access, name, descriptor, signature, value);
	}

	/**
	 * delete Method by don't forward a method call
	 * if the visitXxx has an return value, just return null
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		if (methodName.equals(name)) {
			return null;
		}
		return super.visitMethod(access, name, descriptor, signature, exceptions);
	}
}
