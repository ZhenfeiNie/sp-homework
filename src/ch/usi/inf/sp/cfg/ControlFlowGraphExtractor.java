package ch.usi.inf.sp.cfg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;


/**
 * This class extracts a control flow graph in .dot format
 * from the byte code of a Java method.
 * 
 * @author Zhenfei Nie
 * 
 */
public final class ControlFlowGraphExtractor {
	// used to divide blocks, if true then means there is a block starting from here.
	private boolean [] flags;
	// tracing the current and last block
	private int currentBlockStartLine = 0;
	private int lastBlockStartLine = 0;
	// record all the edges
	private String edges = "";
	// flag that identifies the first insc of a block; used to avoid a '|' before the first insc of a block
	private boolean isTheFirstIsncInBlock = true;
	// judge whether the last insc of last block is a jump insc or a 'normal' insc
	private boolean isLastIsAJumpCode  = false;
	

	public static void main(final String[] args) throws FileNotFoundException, IOException {
		final String classFileName = "/Users/niezhenfei/Documents/JavaWorkspace/SPLA03/bin/ExampleClass.class";//args[0];
		final String methodNameAndDescriptor = "forMethod(I)I";//args[1];
		final ClassReader cr = new ClassReader(new FileInputStream(classFileName));
		final ClassNode clazz = new ClassNode();
		cr.accept(clazz, 0);
		
		final List<MethodNode> methods = clazz.methods;
		// whether the method is existed or not
		boolean flag = false;
		MethodNode method = null;
		for (MethodNode m : methods) {
			final String methodNAD = m.name+m.desc;
			if ( methodNameAndDescriptor.equals(methodNAD) ) {
				method = m;
				flag = true;
				break;
			}
		}
		
		if ( flag == false ) {
			System.err.println("Method `" + methodNameAndDescriptor + "` not found.");
			System.exit(-1);
		}
		
		final ControlFlowGraphExtractor extrator = new ControlFlowGraphExtractor();
		extrator.extractGragph(clazz, method);
	}
	
	/**
	 * Extract the graph file. 
	 * First, it tries to divide the bytecode into blocks by calling divideBlocks(..);
	 * Then, calling analysisInstruction(..) to generate the code for a .dot file.
	 * @param clazz
	 * @param method
	 */
	public void extractGragph( ClassNode clazz, MethodNode method ) {
		System.out.println("Class: "+clazz.name);
		System.out.println("    ==> Method: "+method.name+method.desc);
		divideBlocks(clazz, method);
		
		final InsnList instructions = method.instructions;
		
		
		// generate code 
		StringBuffer commandString = new StringBuffer();
		commandString.append("digraph " + "chals" + " {\n    S [label=\"S\"]\n    E [label=\"E\"]\n");	
		for (int i=0; i<instructions.size(); i++) {
			final AbstractInsnNode instruction = instructions.get(i);
			commandString.append( analysisInstruction(instruction, i, instructions) );
			
		}
		commandString.append("}\"]\n");
		commandString.append( makeAnEdge("S", 0) );
		commandString.append(edges);
		commandString.append("\n}");
		// end of generation
		
		// write .dot file
		
		try {
			File file = new File("output.dot");
	        BufferedWriter output;
			output = new BufferedWriter(new FileWriter(file));
			output.write(commandString.toString());
	        output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Helper method that returns a title for a block.
	 * @param i The first line of a block.s
	 * @return
	 */
	private String makeBlockTitle(final int i) {
		return "B" + i;
	}
	
	/**
	 * Helper methods that link two blocks.
	 * @param start
	 * @param end
	 * @param label
	 * @return
	 */
	private String makeAnEdge(final int start, final int end, String label) {
		label = " [label=\"" + label + "\"]";
		return "\n    " + makeBlockTitle(start) + " -> " + makeBlockTitle(end) + label;
	}
	/**
	 * Helper methods that link two blocks.
	 * @param start
	 * @param end
	 * @return
	 */
	private String makeAnEdge(final int start, final int end) {
		return "\n    " + makeBlockTitle(start) + " -> " + makeBlockTitle(end);
	}
	/**
	 * Helper methods that link two blocks.
	 * @param start
	 * @param endTitle
	 * @return
	 */
	private String makeAnEdge(final int start, final String endTitle) {
		return "\n    " + makeBlockTitle(start) + " -> " + endTitle;
	}
	/**
	 * Helper methods that link two blocks.
	 * @param startTitle
	 * @param end
	 * @return
	 */
	private String makeAnEdge(final String startTitle, final int end) {
		return "\n    " + startTitle + " -> " + makeBlockTitle(end);
	}
	
	
	/**
	 * Analysis the bytecode and generate the .dot content.
	 * @param instruction
	 * @param i
	 * @param instructions
	 * @return
	 */
	public String analysisInstruction(final AbstractInsnNode instruction, final int i, final InsnList instructions) {
		StringBuffer currentInstruction = new StringBuffer();
		boolean isNewBlock = false;
		
		if ( flags[i] ) {
			// new block start
			if ( i != 0 ) {
				currentInstruction.append(" }\"]\n");
			}
			isNewBlock = true;
			isTheFirstIsncInBlock = true;
			lastBlockStartLine = currentBlockStartLine;
			currentBlockStartLine = i;
			currentInstruction.append("    " + makeBlockTitle(currentBlockStartLine) + " [shape=record, label=\"" 
									+ makeBlockTitle(currentBlockStartLine) + "| {");
			
			if ( ! isLastIsAJumpCode && i != 0) {
				edges += this.makeAnEdge(lastBlockStartLine, currentBlockStartLine);
			}
			
			System.out.println("#### ---------------------------------- Block " + currentBlockStartLine);
		} 
		
		// start
		final int opcode = instruction.getOpcode();
		final String mnemonic_origin = opcode==-1?"": i + ": " + Printer.OPCODES[instruction.getOpcode()];
		String mnemonic = opcode==-1?"": i + ": " + Printer.OPCODES[instruction.getOpcode()];
		if ( mnemonic.isEmpty() ) {
			return currentInstruction.toString();
		}
		if ( ! isNewBlock && ! isTheFirstIsncInBlock ) {
			mnemonic = " | " + mnemonic;
		}
		isTheFirstIsncInBlock = false;
		System.out.print( mnemonic + " " );
		
		
		isLastIsAJumpCode = false;
		switch (instruction.getType()) {
		case AbstractInsnNode.LABEL: 
			// pseudo-instruction (branch or exception target)
			System.out.print("// label");
			break;
		case AbstractInsnNode.FRAME:
			// pseudo-instruction (stack frame map)
			System.out.print("// stack frame map");
			break;
		case AbstractInsnNode.LINE:
			// pseudo-instruction (line number information)
			System.out.print("// line number information");
		case AbstractInsnNode.INSN:
			// Opcodes: NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2,
		    // ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0,
			// FCONST_1, FCONST_2, DCONST_0, DCONST_1, IALOAD, LALOAD, FALOAD,
			// DALOAD, AALOAD, BALOAD, CALOAD, SALOAD, IASTORE, LASTORE, FASTORE,
			// DASTORE, AASTORE, BASTORE, CASTORE, SASTORE, POP, POP2, DUP,
			// DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP, IADD, LADD, FADD,
			// DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV,
			// FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG, DNEG, ISHL,
			// LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR,
			// I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B,
			// I2C, I2S, LCMP, FCMPL, FCMPG, DCMPL, DCMPG, IRETURN, LRETURN,
			// FRETURN, DRETURN, ARETURN, RETURN, ARRAYLENGTH, ATHROW,
			// MONITORENTER, or MONITOREXIT.
			// zero operands, nothing to print
			currentInstruction.append(mnemonic);
			if ( mnemonic.endsWith("RETURN") ) {
				edges += this.makeAnEdge(currentBlockStartLine, "E");
				this.isLastIsAJumpCode = true;
			}
			break;
		case AbstractInsnNode.INT_INSN:
			// Opcodes: NEWARRAY, BIPUSH, SIPUSH.
			if (instruction.getOpcode()==Opcodes.NEWARRAY) {
				// NEWARRAY
				System.out.println(Printer.TYPES[((IntInsnNode)instruction).operand]);
				currentInstruction.append(mnemonic + " " + Printer.TYPES[((IntInsnNode)instruction).operand]);
			} else {
				// BIPUSH or SIPUSH
				System.out.println(((IntInsnNode)instruction).operand);
				currentInstruction.append(mnemonic + " " + ((IntInsnNode)instruction).operand);
			}
			
			break;
		case AbstractInsnNode.JUMP_INSN:
			// Opcodes: IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,
		    // IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ,
		    // IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
		{
			final LabelNode targetInstruction = ((JumpInsnNode)instruction).label;
			final int targetId = instructions.indexOf(targetInstruction);
			System.out.print(targetId);
			
			currentInstruction.append( mnemonic + " " + targetId);
			if ( mnemonic_origin.contains("GOTO") || mnemonic_origin.contains("JSR") )  {
				edges += makeAnEdge( currentBlockStartLine, targetId);
			} else {
				edges += makeAnEdge( currentBlockStartLine, targetId, "T" );
				edges += makeAnEdge( currentBlockStartLine, i+1, "F" );
			}
			isLastIsAJumpCode = true;
			break;
		}
		case AbstractInsnNode.LDC_INSN:
			// Opcodes: LDC.
			System.out.print(((LdcInsnNode)instruction).cst);
			currentInstruction.append(mnemonic + " " + ((LdcInsnNode)instruction).cst);
			
			break;
		case AbstractInsnNode.IINC_INSN:
			// Opcodes: IINC.
			System.out.print(((IincInsnNode)instruction).var);
			System.out.println(" ");
			System.out.print(((IincInsnNode)instruction).incr);
			currentInstruction.append(mnemonic + " " + ((IincInsnNode)instruction).var + " " + ((IincInsnNode)instruction).incr);
			break;
		case AbstractInsnNode.TYPE_INSN:
			// Opcodes: NEW, ANEWARRAY, CHECKCAST or INSTANCEOF.
			System.out.print(((TypeInsnNode)instruction).desc);
			currentInstruction.append(mnemonic + " " + ((TypeInsnNode)instruction).desc);
			break;
		case AbstractInsnNode.VAR_INSN:
			// Opcodes: ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE,
		    // LSTORE, FSTORE, DSTORE, ASTORE or RET.
			System.out.print(((VarInsnNode)instruction).var);
			currentInstruction.append(mnemonic + " " + ((VarInsnNode)instruction).var);
			break;
		case AbstractInsnNode.FIELD_INSN:
			// Opcodes: GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD.
			System.out.print(((FieldInsnNode)instruction).owner);
			System.out.print(".");
			System.out.print(((FieldInsnNode)instruction).name);
			System.out.print(" ");
			System.out.print(((FieldInsnNode)instruction).desc);
			currentInstruction.append(mnemonic + " " + ((FieldInsnNode)instruction).owner + "." + ((FieldInsnNode)instruction).name + " " 
							+ ((FieldInsnNode)instruction).desc);
			break;
		case AbstractInsnNode.METHOD_INSN:
			// Opcodes: INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC,
		    // INVOKEINTERFACE or INVOKEDYNAMIC.
			System.out.print(((MethodInsnNode)instruction).owner);
			System.out.print(".");
			System.out.print(((MethodInsnNode)instruction).name);
			System.out.print(" ");
			System.out.print(((MethodInsnNode)instruction).desc);
			currentInstruction.append(mnemonic + " " + ((MethodInsnNode)instruction).owner + "." + ((MethodInsnNode)instruction).name + " "
							+ ((MethodInsnNode)instruction).desc);
			break;
		case AbstractInsnNode.MULTIANEWARRAY_INSN:
			// Opcodes: MULTIANEWARRAY.
			System.out.print(((MultiANewArrayInsnNode)instruction).desc);
			System.out.print(" ");
			System.out.print(((MultiANewArrayInsnNode)instruction).dims);
			currentInstruction.append(mnemonic + " " + ((MultiANewArrayInsnNode)instruction).desc + " " + ((MultiANewArrayInsnNode)instruction).dims);
			break;
		case AbstractInsnNode.LOOKUPSWITCH_INSN:
			// Opcodes: LOOKUPSWITCH.
		{
			String str = mnemonic + " ";
			final List keys = ((LookupSwitchInsnNode)instruction).keys;
			final List labels = ((LookupSwitchInsnNode)instruction).labels;
			for (int t=0; t<keys.size(); t++) {
				final int key = (Integer)keys.get(t);
				final LabelNode targetInstruction = (LabelNode)labels.get(t);
				final int targetId = instructions.indexOf(targetInstruction);				
				System.out.print(key+": "+targetId+", ");	
				str += key+": "+targetId+", ";
				edges += makeAnEdge( currentBlockStartLine, targetId, String.valueOf(key) );
			}
			final LabelNode defaultTargetInstruction = ((LookupSwitchInsnNode)instruction).dflt;
			final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
			System.out.print("default: "+defaultTargetId);		
			
			
			currentInstruction.append( str );
			currentInstruction.append("default: "+defaultTargetId);
			edges += makeAnEdge( currentBlockStartLine, defaultTargetId, "default" );
			isLastIsAJumpCode = true;
			break;
		}
		case AbstractInsnNode.TABLESWITCH_INSN:
			// Opcodes: TABLESWITCH.
		{
			String str = mnemonic + " ";
			final int minKey = ((TableSwitchInsnNode)instruction).min;
			final List labels = ((TableSwitchInsnNode)instruction).labels;
			for (int t=0; t<labels.size(); t++) {
				final int key = minKey+t;
				final LabelNode targetInstruction = (LabelNode)labels.get(t);
				final int targetId = instructions.indexOf(targetInstruction);
				System.out.print(key+": "+targetId+", ");
				str += key+": "+targetId+", ";
				edges += makeAnEdge( currentBlockStartLine, targetId, String.valueOf(key) );
			}
			final LabelNode defaultTargetInstruction = ((TableSwitchInsnNode)instruction).dflt;
			final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
			
			currentInstruction.append( str );
			currentInstruction.append("default: "+defaultTargetId);
			
			edges += makeAnEdge( currentBlockStartLine, defaultTargetId, "default" );
			isLastIsAJumpCode = true;
			break;
		}
		}		
		System.out.println();
		
		return currentInstruction.toString();
	}
	
	/**
	 * This method is to divide the bytecode into blocks.
	 * @param clazz
	 * @param method
	 */
	private final void divideBlocks(ClassNode clazz, MethodNode method) {
		final InsnList instructions = method.instructions;
		flags = new boolean[instructions.size()];
		for ( int i=0; i<flags.length; i++ ) {
			flags[i] = false;
		}
		flags[0] = true;
		for (int i=0; i<instructions.size(); i++) {
			final AbstractInsnNode instruction = instructions.get(i);	
			devide(instruction, i, instructions);
		}
		
	}
	
	/**
	 * This method is to execute the dividing action.
	 * @param instruction
	 * @param i
	 * @param instructions
	 */
	private final void devide(final AbstractInsnNode instruction, final int i, final InsnList instructions){
		
		final int opcode = instruction.getOpcode();
		String mnemonic = opcode==-1?"":Printer.OPCODES[instruction.getOpcode()];
	
		
		switch (instruction.getType()) {
		case AbstractInsnNode.INSN:
			// Opcodes: NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2,
		    // ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0,
			// FCONST_1, FCONST_2, DCONST_0, DCONST_1, IALOAD, LALOAD, FALOAD,
			// DALOAD, AALOAD, BALOAD, CALOAD, SALOAD, IASTORE, LASTORE, FASTORE,
			// DASTORE, AASTORE, BASTORE, CASTORE, SASTORE, POP, POP2, DUP,
			// DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP, IADD, LADD, FADD,
			// DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV,
			// FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG, FNEG, DNEG, ISHL,
			// LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR,
			// I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B,
			// I2C, I2S, LCMP, FCMPL, FCMPG, DCMPL, DCMPG, IRETURN, LRETURN,
			// FRETURN, DRETURN, ARETURN, RETURN, ARRAYLENGTH, ATHROW,
			// MONITORENTER, or MONITOREXIT.
			// zero operands, nothing to print
			if ( mnemonic.endsWith("RETURN") ) {
				flags[i+1] = true;
			}
			
			
			break;
		case AbstractInsnNode.JUMP_INSN:
			// Opcodes: IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,
		    // IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ,
		    // IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
		{
			final LabelNode targetInstruction = ((JumpInsnNode)instruction).label;
			final int targetId = instructions.indexOf(targetInstruction);
			flags[targetId] = true;
			flags[i+1] = true;
			break;
		}	
		case AbstractInsnNode.LOOKUPSWITCH_INSN:
			// Opcodes: LOOKUPSWITCH.
		{
			final List keys = ((LookupSwitchInsnNode)instruction).keys;
			final List labels = ((LookupSwitchInsnNode)instruction).labels;
			for (int t=0; t<keys.size(); t++) {
				final int key = (Integer)keys.get(t);
				final LabelNode targetInstruction = (LabelNode)labels.get(t);
				final int targetId = instructions.indexOf(targetInstruction);
				flags[targetId] = true;
				
			}
			final LabelNode defaultTargetInstruction = ((LookupSwitchInsnNode)instruction).dflt;
			final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
			flags[defaultTargetId] = true;
			break;
		}
		case AbstractInsnNode.TABLESWITCH_INSN:
			// Opcodes: TABLESWITCH.
		{
			final int minKey = ((TableSwitchInsnNode)instruction).min;
			final List labels = ((TableSwitchInsnNode)instruction).labels;
			for (int t=0; t<labels.size(); t++) {
				final int key = minKey+t;
				final LabelNode targetInstruction = (LabelNode)labels.get(t);
				final int targetId = instructions.indexOf(targetInstruction);
				flags[targetId] = true;
			}
			final LabelNode defaultTargetInstruction = ((TableSwitchInsnNode)instruction).dflt;
			final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
			flags[defaultTargetId] = true;
			break;
		}
		}		
	}
}
