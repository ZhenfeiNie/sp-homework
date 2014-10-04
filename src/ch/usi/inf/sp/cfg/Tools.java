package ch.usi.inf.sp.cfg;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.util.Printer;

public class Tools {

	public static String getMnemonic(AbstractInsnNode ins) {
		String mnemonic = null;
		
		switch (ins.getType()) {
		case AbstractInsnNode.LABEL: 
//			mnemonic = "label";
			break;
		case AbstractInsnNode.FRAME:
//			mnemonic = "stack frame map";
			break;
		case AbstractInsnNode.LINE:
//			mnemonic = "line number information";
			break;
		default:
			mnemonic = Printer.OPCODES[ins.getOpcode()];
		}
		return mnemonic;
	}
	
	public static boolean isRealInsn(AbstractInsnNode ins) {
		boolean isReal = true;
		final int opcode = ins.getOpcode();
		if ( opcode == -1 ) {
			isReal = false;
		}
		switch (ins.getType()) {
		case AbstractInsnNode.LABEL: 
//			mnemonic = "label";
			isReal = false;
			break;
		case AbstractInsnNode.FRAME:
//			mnemonic = "stack frame map";
			isReal = false;
			break;
		case AbstractInsnNode.LINE:
//			mnemonic = "line number information";
			isReal = false;
			break;
		default:
			isReal = true;
		}
		return isReal;
	}
}
