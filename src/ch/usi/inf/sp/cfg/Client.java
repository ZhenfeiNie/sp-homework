package ch.usi.inf.sp.cfg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class Client {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		final String classFileName = "/Users/niezhenfei/Documents/JavaWorkspace/SPLA03/bin/ExampleClass.class";//args[0];
		final String methodNameAndDescriptor = "forMethod(I)I";//args[1];
		final ClassReader cr = new ClassReader(new FileInputStream(classFileName));
		final ClassNode clazz = new ClassNode();
		cr.accept(clazz, 0);
		
		@SuppressWarnings("unchecked")
		final List<MethodNode> methods = clazz.methods;
		final ControlFlowGraphCreator creator = new ControlFlowGraphCreator();
		ControlFlowGraph cfg = null;
		for (MethodNode m : methods) {
			final String methodNAD = m.name + m.desc;
			if ( methodNameAndDescriptor.equals(methodNAD) ) {
				cfg = creator.create(m);
			}
		}
		System.out.println( cfg.generateDot() );
		
		System.out.println( " ------------- " );
		
		DominatorAnalysis da = new ZNDominatorAnalysis();
		DiGraph dt =  da.analyse(cfg);
		System.out.println( dt.generateDot() );
		


	}

}
