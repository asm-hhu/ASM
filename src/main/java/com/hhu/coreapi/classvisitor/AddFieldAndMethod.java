package com.hhu.coreapi.classvisitor;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static jdk.internal.org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * @author zhangji
 */
public class AddFieldAndMethod extends ClassVisitor {

	private  String fieldName;
	private boolean isFieldPresent;

	public AddFieldAndMethod(int api) {
		super(api);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		if (fieldName.equals(name)) {
			isFieldPresent = true;
		}
		return super.visitField(access, name, descriptor, signature, value);
	}

	/**
	 * add Field/Method in visitEnd()
	 * because visitEnd() only and must called once
	 */
	@Override
	public void visitEnd() {
		if (!isFieldPresent) {
			// descriptor can also be the int(I), double(D) and so on.
			String descriptor = "Ljava.lang.String;";
			FieldVisitor fv = cv.visitField(ACC_PUBLIC, fieldName, descriptor, null, null);
		}
		super.visitEnd();
	}
}
