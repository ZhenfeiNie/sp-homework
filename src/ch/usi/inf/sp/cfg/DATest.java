package ch.usi.inf.sp.cfg;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class DATest {

	@Test
	public void testIPred() throws FileNotFoundException, IOException {
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
		
		ZNDominatorAnalysis da = new ZNDominatorAnalysis();
		Block b = cfg.blocks.get(0);
		Set<Block> preds = da.getImmediatePredecessors(b, cfg);
		
		
		fail("Not yet implemented");
	}
	
//	
//
//	System.out.println(cfg.blocks.get(0) + " " + cfg.clone().blocks.get(0));
//	System.out.println(cfg.blocks.get(0) == cfg.clone().blocks.get(0));
//	System.out.println(cfg.blocks.get(0).hashCode() == cfg.clone().blocks.get(0).hashCode());
//	System.out.println(cfg.blocks.get(0).equals(cfg.clone().blocks.get(0)));
//	
//	System.out.println((new String("123")).hashCode() == (new String("123")).hashCode());
	
//	ZNDominatorAnalysis da = new ZNDominatorAnalysis();
//	for ( int i=0; i<cfg.blocks.size(); i++ ) {
//		Block b = cfg.blocks.get(i);
//		Set<Block> s = da.getImmediatePredecessors(b, cfg);
//		System.out.println(b + " " + s.size() + " " + s.toString());
//		System.out.println( cfg.blocks.contains(b) );
//		
//	}
//	
//	Map<Block, Set<Block>> domSets = da.initDoms(cfg);
//	for (Block b : domSets.keySet()) {
//		System.out.print("@ " + b + " :: ");
//		for ( Block dom : domSets.get(b) ) {
//			System.out.print(" " + dom);
//		}
//		System.out.println();
//	}
//	
//	System.out.println("\n GOT ");
//	Block b3 = cfg.blocks.get(3);
//	Block b4 = cfg.blocks.get(4);
//	Set<Block> s3 = domSets.get(b3);
//	Set<Block> s4 = domSets.get(b4);
//	
//	System.out.println(da.gotChanged(s3, s4));
//	s4.remove(b4);
//	System.out.println(da.gotChanged(s3, s4));
//	
//	System.out.println("\n========");
//	System.out.println("S3: " + s3.toString());
//	System.out.println("S4: " + s4.toString());
//	s4.clear();
//	Set<Block> si = da.calcuteIntersetion(s3, s4);
//	System.out.println("SI: " + si.toString());

}
