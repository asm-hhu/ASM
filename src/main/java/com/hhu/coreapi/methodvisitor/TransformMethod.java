package com.hhu.coreapi.methodvisitor;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NOP;

public class TransformMethod extends MethodVisitor {

	public TransformMethod(int api) {
		super(api);
	}

	/**
	 * transformer instructions inside Method
	 * @param opcode is the byte code about the code in the method
	 */
	@Override
	public void visitInsn(int opcode) {
		if (opcode == NOP) {
			opcode = IRETURN;
			super.visitInsn(opcode);
		}
		super.visitInsn(opcode);
	}

}
