package ch.usi.inf.sp.cfg;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.util.Printer;

/**
 * 
 * @author niezhenfei
 *
 */
public class Tools {

	public static String getMnemonic(AbstractInsnNode ins) {
		String mnemonic = null;
		
		switch (ins.getType()) {
		case AbstractInsnNode.LABEL: 
			break;
		case AbstractInsnNode.FRAME:
			break;
		case AbstractInsnNode.LINE:
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
			isReal = false;
			break;
		case AbstractInsnNode.FRAME:
			isReal = false;
			break;
		case AbstractInsnNode.LINE:
			isReal = false;
			break;
		default:
			isReal = true;
		}
		return isReal;
	}
}
