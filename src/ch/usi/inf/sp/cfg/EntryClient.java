package ch.usi.inf.sp.cfg;

import java.io.FileInputStream;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Please use this as the entry of the program.
 * @author Zhenfei Nie <zhen.fei.nie@usi.ch>
 *
 */
public class EntryClient {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		final String classFileName = "/Users/niezhenfei/Documents/JavaWorkspace/SPLA03/bin/ExampleClass.class";//args[0];
		final String methodNameAndDescriptor = "throwExceptionNested()V";//args[1];
		final ClassReader cr = new ClassReader(new FileInputStream(classFileName));
		final ClassNode clazz = new ClassNode();
		cr.accept(clazz, 0);
		
		@SuppressWarnings("unchecked")
		final List<MethodNode> methods = clazz.methods;
		final ControlFlowGraphExtractor creator = new ControlFlowGraphExtractor();
		ControlFlowGraph cfg = null;
		for (MethodNode m : methods) {
			final String methodNAD = m.name + m.desc;
			if ( methodNameAndDescriptor.equals(methodNAD) ) {
				cfg = creator.create(m);
			}
		}
		if ( cfg == null ) {
			System.out.println( "NULL" );
			return;
		}
//		System.out.println( cfg.generateDot() );
		
		DominatorAnalysis da = new ZNDominatorAnalysis();
		DiGraph dt =  da.analyse(cfg);
		System.out.println( dt.generateDot() );
		
//		System.out.println(creator.exceptionTable);
//		
//		for ( ExceptionEdge ee : cfg.exceptionEdges )  {
//			System.err.print(ee.generateDot());
//		}
//
//		int loc = creator.exceptionTable.search(6, "Error");
//		System.out.println(loc);
		
//		System.out.println("\n\n------------------------------------\n");
//		for ( AbstractInsnNode ins : creator.typeMap.keySet()) {
//			System.err.println( Tools.getMnemonic(ins, cfg.blocks.get(3).originList));
//			if ( !creator.isPEIIns(ins) ) {
//				continue;
//			}
//			System.err.println(creator.typeMap.get(ins));
//		}
		
	}

}
