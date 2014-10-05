package ch.usi.inf.sp.cfg;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;

/**
 * 
 * @author niezhenfei
 *
 */
public class Tools {

	public static String getMnemonic(AbstractInsnNode ins, InsnList instructions) {
		String mnemonic = null;
		if (ins.getOpcode() == -1) {
			return null;
		}
		mnemonic = Printer.OPCODES[ins.getOpcode()];
		switch (ins.getType()) {
		case AbstractInsnNode.LABEL: 
			mnemonic = null;
			break;
		case AbstractInsnNode.FRAME:
			mnemonic = null;
			break;
		case AbstractInsnNode.LINE:
			mnemonic = null;
			break;
		case AbstractInsnNode.INT_INSN:
			if (ins.getOpcode()==Opcodes.NEWARRAY) {
				mnemonic += " " + Printer.TYPES[((IntInsnNode)ins).operand];
			} else {
				mnemonic += ((IntInsnNode)ins).operand;
			}
			break;
		case AbstractInsnNode.JUMP_INSN:
		{
			final LabelNode targetInstruction = ((JumpInsnNode)ins).label;
			final int targetId = instructions.indexOf(targetInstruction);
			mnemonic += " " + targetId;
			break;
		}
		case AbstractInsnNode.LDC_INSN:
			mnemonic += " " + ((LdcInsnNode)ins).cst;
			break;
		case AbstractInsnNode.IINC_INSN:
			mnemonic += " " + ((IincInsnNode)ins).var + " " + ((IincInsnNode)ins).incr;
			break;
		case AbstractInsnNode.TYPE_INSN:
			mnemonic += " " + ((TypeInsnNode)ins).desc;
			break;
		case AbstractInsnNode.VAR_INSN:
			mnemonic += " " + ((VarInsnNode)ins).var;
			break;
		case AbstractInsnNode.FIELD_INSN:
			mnemonic += " " + ((FieldInsnNode)ins).owner;
			mnemonic += ".";
			mnemonic += ((FieldInsnNode)ins).name;
			mnemonic += " ";
			mnemonic += ((FieldInsnNode)ins).desc;
			break;
		case AbstractInsnNode.METHOD_INSN:
			mnemonic += " " + ((MethodInsnNode)ins).owner;
			mnemonic += ".";
			mnemonic += ((MethodInsnNode)ins).name;
			mnemonic += " ";
			mnemonic += ((MethodInsnNode)ins).desc;
			break;
		case AbstractInsnNode.MULTIANEWARRAY_INSN:
			mnemonic += " " + ((MultiANewArrayInsnNode)ins).desc;
			mnemonic += " ";
			mnemonic += ((MultiANewArrayInsnNode)ins).dims;
			break;
		case AbstractInsnNode.LOOKUPSWITCH_INSN:
		{
			@SuppressWarnings("rawtypes")
			final List keys = ((LookupSwitchInsnNode)ins).keys;
			@SuppressWarnings("rawtypes")
			final List labels = ((LookupSwitchInsnNode)ins).labels;
			for (int t=0; t<keys.size(); t++) {
				final int key = (Integer)keys.get(t);
				final LabelNode targetInstruction = (LabelNode)labels.get(t);
				final int targetId = instructions.indexOf(targetInstruction);
				mnemonic = mnemonic + " " + key+": "+targetId+", ";
			}
			final LabelNode defaultTargetInstruction = ((LookupSwitchInsnNode)ins).dflt;
			final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
			mnemonic = mnemonic + " " + "default: " + defaultTargetId;
			break;
		}
		case AbstractInsnNode.TABLESWITCH_INSN:
		{
			final int minKey = ((TableSwitchInsnNode)ins).min;
			@SuppressWarnings("rawtypes")
			final List labels = ((TableSwitchInsnNode)ins).labels;
			for (int t=0; t<labels.size(); t++) {
				final int key = minKey+t;
				final LabelNode targetInstruction = (LabelNode)labels.get(t);
				final int targetId = instructions.indexOf(targetInstruction);
				mnemonic = mnemonic + " " + key+": "+targetId+", ";
			}
			final LabelNode defaultTargetInstruction = ((TableSwitchInsnNode)ins).dflt;
			final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
			mnemonic = mnemonic + " " + "default: " + defaultTargetId;
			break;
		}	
		
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
