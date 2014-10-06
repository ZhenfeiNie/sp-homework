package ch.usi.inf.sp.cfg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * 
 * @author Zhenfei Nie <zhen.fei.nie@usi.ch>
 *
 */
public class ControlFlowGraphExtractor {
	/**
	 * The exception table.
	 */
	public ExceptionTable exceptionTable;

	/**
	 * define the relation between opcodes and exceptions
	 * 
	 */
	public Map<AbstractInsnNode, List<String>> typeMap;
	
	public ControlFlowGraphExtractor() {
		exceptionTable = new ExceptionTable();
	}
	
	public ControlFlowGraph create(MethodNode methodNode) {
		ControlFlowGraph cfg = new ControlFlowGraph(methodNode.name);
		cfg.addEntry();
		cfg.addEnd();
		return createGraph(methodNode, cfg);
	}
	
	/**
	 * 
	 * @param methodNode
	 * @param cfg
	 * @return
	 */
	public ControlFlowGraph createGraph(MethodNode methodNode, ControlFlowGraph cfg) {
		System.out.println("==> Ready for : "+ methodNode.name + methodNode.desc);
		// @1 build the exception table
		buildExceptionTable(methodNode);
		boolean [] flags = divideBlocks(methodNode);
		
		boolean [] isPEI = takeCareOfExceptions(methodNode);
		for ( int i=isPEI.length-1; i>=1; i-- ) {
			isPEI[i] = isPEI[i-1];
		}
		isPEI[0] = false;
		
		for ( int i=0; i< isPEI.length; i++ ) {
			flags[i] = flags[i] || isPEI[i];
		}
		
		return buildBlocks(methodNode, flags, cfg);
	}
	
	/**
	 * Build the exception table of current method.
	 * @param methodNode
	 */
	public void buildExceptionTable(MethodNode methodNode) {
		@SuppressWarnings("unchecked")
		List<TryCatchBlockNode> tcb = methodNode.tryCatchBlocks;
		InsnList instructions = methodNode.instructions;
		for ( int i=0; i<tcb.size(); i++ ) {
			int start, end, handler;
			String type;
			start = instructions.indexOf( tcb.get(i).start );
			end = instructions.indexOf( tcb.get(i).end );
			handler = instructions.indexOf( tcb.get(i).handler );
			type = tcb.get(i).type;
			
			this.exceptionTable.add(start, end, handler, type);
		}
	}
	
	public ControlFlowGraph buildBlocks(MethodNode methodNode, boolean [] flags, ControlFlowGraph cfg) {
		InsnList instructions = methodNode.instructions;
		// build blocks
		// always omit last insn(which is just a '//label')
		Block currentBlock = null;
		for ( int i=0; i<flags.length-1; i++ ) {
			final boolean flag = flags[i];
			if ( flag == false ) {
				currentBlock.addInstruction(instructions.get(i), i);
			} else {
				currentBlock = new Block(i);
				currentBlock.originList = instructions;
				currentBlock.addInstruction(instructions.get(i), i);
				cfg.addBlock(currentBlock);
			}
		}
		// Just print
//		System.out.println(cfg.blocks.size());
//		for ( int i=0; i<cfg.blocks.size(); i++ ) {
//			Block b = cfg.blocks.get(i);
//			System.out.println(i + " ==> " + b.getTitle() );
//			for ( int j=0; j<b.instructions.size(); j++)  {
//				AbstractInsnNode ins = b.instructions.get(j);
//				int index = b.indices.get(j);
//				System.out.println("    " + index + " : " + Tools.getMnemonic(ins, b.originList));
//			}
//		}
//		System.out.println("\n\n\n" );
		
		// connect all edges
		for ( int i=0; i<cfg.blocks.size(); i++) {
			Block b = cfg.blocks.get(i);
			AbstractInsnNode ins = b.getLastInsn();
			// if it is null then it should be either a start block or end block
			if ( ins == null ) {
				continue;
			}
			
			if ( b.index == 0 ) {
				cfg.addEdge(new Edge(cfg.entry, b, ""));
			}
			
			// BEGIN: connect exception edges (if any) --------------------
			List<String> ex = null;
			if ( isPEIIns(ins) ) {
//				System.out.println("PEI: " + instructions.indexOf(ins) + " " + Tools.getMnemonic(ins, instructions));
				ex = this.typeMap.get(ins);
				if ( ex != null ) {
//					System.out.println(Tools.getMnemonic(ins, instructions) + " " + ex.toString());
					int [] handlers = new int[ex.size()]; 
					for ( int j=0; j<handlers.length; j++ ) {
						handlers[j] = this.exceptionTable.search(instructions.indexOf(ins), ex.get(j));
						if ( handlers[j] == -2 ) {
							cfg.addExceptionEdge(new ExceptionEdge(b, cfg.end, ""));
						} else {
							Block handlerBlock = cfg.findBlockByInsn(handlers[j]);
							cfg.addExceptionEdge(new ExceptionEdge(b, handlerBlock, ex.get(j)));
						}
					}
				}
			}
			// END  --------------------
			
			// normal edges
			switch (ins.getType()) {
			case AbstractInsnNode.INSN:
				if ( ins.getOpcode() == Opcodes.ATHROW ) {
					// xian bu chu li
				} else if ( ins.getOpcode() >= Opcodes.IRETURN && ins.getOpcode() <= Opcodes.RETURN ) {
					cfg.addEdge( new Edge( b, cfg.end, "" ) );
				} else {
					
					if ( i < cfg.blocks.size() - 1 ) {
						Block falldown = cfg.blocks.get(i+1);
						cfg.addEdge( new Edge( b, falldown, "" ) );
					} else {
						cfg.addEdge( new Edge( b, cfg.end, "" ) );
					}
				}
				break;
			case AbstractInsnNode.JUMP_INSN:
			{	
				final LabelNode targetInstruction = ((JumpInsnNode)ins).label;
				final int targetId = instructions.indexOf(targetInstruction);
				Block target = cfg.findBlockByInsn(targetId);
				if ( target != null ) {
					if ( ins.getOpcode() != Opcodes.GOTO  && ins.getOpcode() != Opcodes.JSR ) {
						Block falldown = cfg.blocks.get(i+1);
						cfg.addEdge( new Edge( b, target, "T" ) );
						cfg.addEdge( new Edge( b, falldown, "F" ) );
					} else {
						cfg.addEdge( new Edge( b, target, "" ) );
					}
				} else {
					//
				}
				break;
			}
			case AbstractInsnNode.LOOKUPSWITCH_INSN:
			{
				@SuppressWarnings("rawtypes")
				final List keys = ((LookupSwitchInsnNode)ins).keys;
				@SuppressWarnings("rawtypes")
				final List labels = ((LookupSwitchInsnNode)ins).labels;
				for (int t=0; t<keys.size(); t++) {
					final LabelNode targetInstruction = (LabelNode)labels.get(t);
					final int targetId = instructions.indexOf(targetInstruction);
					final int key = (Integer)keys.get(t);
					Block target = cfg.findBlockByInsn(targetId);
					if ( target != null ) {
						cfg.addEdge( new Edge( b, target, String.valueOf(key) ) );
					} else {
						//
					}
				}
				final LabelNode defaultTargetInstruction = ((LookupSwitchInsnNode)ins).dflt;
				final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
				
				Block defaultBlock = cfg.findBlockByInsn(defaultTargetId);
				if ( defaultBlock != null ) {
					cfg.addEdge( new Edge( b, defaultBlock, "default" ) );
				} else {
					//
				}
				break;
			}
			case AbstractInsnNode.TABLESWITCH_INSN:
				// Opcodes: TABLESWITCH.
			{
				@SuppressWarnings("rawtypes")
				final List keys = ((LookupSwitchInsnNode)ins).keys;
				@SuppressWarnings("rawtypes")
				final List labels = ((TableSwitchInsnNode)ins).labels;
				for (int t=0; t<labels.size(); t++) {
					final LabelNode targetInstruction = (LabelNode)labels.get(t);
					final int targetId = instructions.indexOf(targetInstruction);
					final int key = (Integer)keys.get(t);
					Block target = cfg.findBlockByInsn(targetId);
					if ( target != null ) {
						cfg.addEdge( new Edge( b, target, String.valueOf(key) ) );
					} else {
						//
					}
				}
				final LabelNode defaultTargetInstruction = ((TableSwitchInsnNode)ins).dflt;
				final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
				
				Block defaultBlock = cfg.findBlockByInsn(defaultTargetId);
				if ( defaultBlock != null ) {
					cfg.addEdge( new Edge( b, defaultBlock, "default" ) );
				} else {
					//
				}
				break;
			}
			default:
				if ( i < cfg.blocks.size() - 1 ) {
					Block falldown = cfg.blocks.get(i+1);
					cfg.addEdge( new Edge( b, falldown, "" ) );
				} else {
					cfg.addEdge( new Edge( b, cfg.end, "" ) );
				}
			}		
		}
		
		return cfg;
	}
	
	public boolean isPEIIns(AbstractInsnNode ain) {
		switch (ain.getOpcode()) {
        case Opcodes.AALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.AASTORE: // NullPointerException, ArrayIndexOutOfBoundsException, ArrayStoreException
        case Opcodes.ANEWARRAY: // NegativeArraySizeException, (linking)
        case Opcodes.ARETURN: // IllegalMonitorStateException (if synchronized)
        case Opcodes.ARRAYLENGTH: // NullPointerException
        case Opcodes.ATHROW: // NullPointerException, IllegalMonitorStateException (if synchronized), 
        case Opcodes.BALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.BASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.CALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.CASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.CHECKCAST: // ClassCastException, (linking)
        case Opcodes.DALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.DASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.DRETURN: // IllegalMonitorStateException (if synchronized)
        case Opcodes.FALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.FASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.FRETURN: // IllegalMonitorStateException (if synchronized)
        case Opcodes.GETFIELD: // NullPointerException, (linking)
        case Opcodes.GETSTATIC: // Error*, (linking)
        case Opcodes.IALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.IASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.IDIV: // ArithmeticException
        case Opcodes.INSTANCEOF: // (linking)
        case Opcodes.INVOKEDYNAMIC: // what's this??
        case Opcodes.INVOKEINTERFACE: // NullPointerException, IncompatibleClassChangeError, AbstractMethodError, IllegalAccessError, AbstractMethodError, UnsatisfiedLinkError, (linking)
        case Opcodes.INVOKESPECIAL: // NullPointerException, UnsatisfiedLinkError, (linking)
        case Opcodes.INVOKESTATIC: // UnsatisfiedLinkError, Error*, (linking)
        case Opcodes.INVOKEVIRTUAL: // NullPointerException, AbstractMethodError, UnsatisfiedLinkError, (linking)
        case Opcodes.IREM: // ArithmeticException
        case Opcodes.IRETURN: // IllegalMonitorStateException (if synchronized)
        case Opcodes.LALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.LASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.LDIV: // ArithmeticException
        case Opcodes.LREM: // ArithmeticException
        case Opcodes.LRETURN: // IllegalMonitorStateException (if synchronized)
        case Opcodes.MONITORENTER: // NullPointerException
        case Opcodes.MONITOREXIT: // NullPointerException, IllegalMonitorStateException
        case Opcodes.MULTIANEWARRAY: // NegativeArraySizeException, (linking)
        case Opcodes.NEW: // Error*, (linking)
        case Opcodes.NEWARRAY: // NegativeArraySizeException
        case Opcodes.PUTFIELD: // NullPointerException, (linking)
        case Opcodes.PUTSTATIC: // Error*, (linking)
        case Opcodes.RETURN: // IllegalMonitorStateException (if synchronized)
        case Opcodes.SALOAD: // NullPointerException, ArrayIndexOutOfBoundsException
        case Opcodes.SASTORE: // NullPointerException, ArrayIndexOutOfBoundsException
                return true;
        default:
        	return false;
		}
	}

	public boolean[] divideBlocks(MethodNode methodNode) {
		InsnList instructions = methodNode.instructions;
		boolean [] flags;
		flags = new boolean[instructions.size()];
		
		for ( int i=1; i<instructions.size(); i++ ) {
			flags[i] = false;
		}
		flags[0] = true;
		
		for ( int i=0; i<instructions.size(); i++ ) {
			AbstractInsnNode ins = instructions.get(i);
			switch (ins.getType()) {
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
				
				if ( ins.getType() == Opcodes.ATHROW ) {
					if ( i < instructions.size() - 1 ) {
						flags[i+1] = true;
					}
				}
				if ( ins.getType() >= Opcodes.IRETURN && ins.getType() <= Opcodes.RETURN ) {
					if ( i < instructions.size() - 1 ) {
						flags[i+1] = true;
					}
				}
				break;
			case AbstractInsnNode.JUMP_INSN:
				// Opcodes: IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,
			    // IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ,
			    // IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
			{
				final LabelNode targetInstruction = ((JumpInsnNode)ins).label;
				final int targetId = instructions.indexOf(targetInstruction);
				flags[targetId] = true;
				if ( i < instructions.size() - 1 ) {
					flags[i+1] = true;
				}
				break;
			}
			case AbstractInsnNode.LOOKUPSWITCH_INSN:
				// Opcodes: LOOKUPSWITCH.
			{
				@SuppressWarnings("rawtypes")
				final List keys = ((LookupSwitchInsnNode)ins).keys;
				@SuppressWarnings("rawtypes")
				final List labels = ((LookupSwitchInsnNode)ins).labels;
				for (int t=0; t<keys.size(); t++) {
					final LabelNode targetInstruction = (LabelNode)labels.get(t);
					final int targetId = instructions.indexOf(targetInstruction);
					flags[targetId] = true;
					
				}
				final LabelNode defaultTargetInstruction = ((LookupSwitchInsnNode)ins).dflt;
				final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
				flags[defaultTargetId] = true;
				break;
			}
			case AbstractInsnNode.TABLESWITCH_INSN:
				// Opcodes: TABLESWITCH.
			{
				@SuppressWarnings("rawtypes")
				final List labels = ((TableSwitchInsnNode)ins).labels;
				for (int t=0; t<labels.size(); t++) {
					final LabelNode targetInstruction = (LabelNode)labels.get(t);
					final int targetId = instructions.indexOf(targetInstruction);
					flags[targetId] = true;
				}
				final LabelNode defaultTargetInstruction = ((TableSwitchInsnNode)ins).dflt;
				final int defaultTargetId = instructions.indexOf(defaultTargetInstruction);
				flags[defaultTargetId] = true;
				break;
			}
			}		
		}
		return flags;
	}
	
	
	public boolean[] takeCareOfExceptions(MethodNode methodNode) {
		InsnList instructions = methodNode.instructions;
		boolean[] isPEI = new boolean[instructions.size()];
		
		typeMap = new HashMap<AbstractInsnNode, List<String>>();
		for ( int i=0; i<instructions.size(); i++ ) {
			List<String> types = null;
			final AbstractInsnNode ins = instructions.get(i);
			switch (ins.getOpcode()) {
			        case Opcodes.AALOAD: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.AASTORE: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException", "ArrayStoreException");
			        	break;
			        case Opcodes.ANEWARRAY:
			        	isPEI[i] = true;
			        	types = Arrays.asList("NegativeArraySizeException");
			        	break;
			        case Opcodes.ARETURN: //  (if synchronized)
			        	isPEI[i] = true;
			        	types = Arrays.asList("IllegalMonitorStateException");
			        	break;
			        case Opcodes.ARRAYLENGTH: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException");
			        	break;
			        case Opcodes.ATHROW:  //  ( 2nd: if synchronized)
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "IllegalMonitorStateException"); 
			        	break;
			        case Opcodes.BALOAD: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.BASTORE: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.CALOAD: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.CASTORE: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.CHECKCAST: // (linking)
			        	isPEI[i] = true;
			        	types = Arrays.asList("ClassCastException");
			        	break;
			        case Opcodes.DALOAD: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.DASTORE: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.DRETURN: //  (if synchronized)
			        	isPEI[i] = true;
			        	types = Arrays.asList("IllegalMonitorStateException");
			        	break;
			        case Opcodes.FALOAD: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.FASTORE: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.FRETURN: //  (if synchronized)
			        	isPEI[i] = true;
			        	types = Arrays.asList("IllegalMonitorStateException");
			        	break;
			        case Opcodes.GETFIELD: //  (linking)
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException");
			        	break;
			        case Opcodes.GETSTATIC: // Error*, (linking)
			        	isPEI[i] = true;
			        	types = Arrays.asList("Error");
			        	break;
			        case Opcodes.IALOAD: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.IASTORE: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.IDIV: // ArithmeticException
			        	isPEI[i] = true;
			        	types = Arrays.asList("ArithmeticException");
			        	break;
			        case Opcodes.INSTANCEOF: // (linking)
			        	isPEI[i] = true;
			        	types = Arrays.asList("instanceof");
			        	break;
			        case Opcodes.INVOKEDYNAMIC: // what's this??
			        	isPEI[i] = true;
			        	types = Arrays.asList("what's this??");
			        	break;
			        case Opcodes.INVOKEINTERFACE: // (linking)
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "IncompatibleClassChangeError", "AbstractMethodError",
			        						"IllegalAccessError", "AbstractMethodError", "UnsatisfiedLinkError");
			        	break;
			        case Opcodes.INVOKESPECIAL: //  (linking)
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "UnsatisfiedLinkError");
			        	break;
			        case Opcodes.INVOKESTATIC: //
			        	isPEI[i] = true;
			        	types = Arrays.asList("UnsatisfiedLinkError", "Error");
			        	break;
			        case Opcodes.INVOKEVIRTUAL: // (linking)
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "AbstractMethodError", "UnsatisfiedLinkError");
			        	break;
			        case Opcodes.IREM: // 
			        	isPEI[i] = true;
			        	types = Arrays.asList("ArithmeticException");
			        	break;
			        case Opcodes.IRETURN: //  (if synchronized)
			        	isPEI[i] = true;
			        	types = Arrays.asList("IllegalMonitorStateException");
			        	break;
			        case Opcodes.LALOAD: // 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.LASTORE: //
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.LDIV: // ArithmeticException
			        	isPEI[i] = true;
			        	types = Arrays.asList("ArithmeticException");
			        	break;
			        case Opcodes.LREM: // ArithmeticException
			        	isPEI[i] = true;
			        	types = Arrays.asList("ArithmeticException");
			        	break;
			        case Opcodes.LRETURN: //  (if synchronized)
			        	isPEI[i] = true;
			        	types = Arrays.asList("IllegalMonitorStateException");
			        	break;
			        case Opcodes.MONITORENTER: // 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException");
			        	break;

			        case Opcodes.MONITOREXIT: // 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "IllegalMonitorStateException");
			        	break;
			        case Opcodes.MULTIANEWARRAY: // (linking)
			        	isPEI[i] = true;
			        	types = Arrays.asList("NegativeArraySizeException");
			        	break;
			        case Opcodes.NEW: // (linking)
			        	isPEI[i] = true;
			        	types = Arrays.asList("Error");
			        	break;
			        case Opcodes.NEWARRAY: // 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NegativeArraySizeException");
			        	break;
			        case Opcodes.PUTFIELD: //  (linking)
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.PUTSTATIC: // Error*, (linking)
			        	isPEI[i] = true;
			        	types = Arrays.asList("Error");
			        	break;
			        case Opcodes.RETURN: //  (if synchronized)
			        	isPEI[i] = true;
			        	types = Arrays.asList("IllegalMonitorStateException");
			        	break;
			        case Opcodes.SALOAD: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			        case Opcodes.SASTORE: 
			        	isPEI[i] = true;
			        	types = Arrays.asList("NullPointerException", "ArrayIndexOutOfBoundsException");
			        	break;
			}
			typeMap.put(ins, types);
		}
		return isPEI;
	}
}
