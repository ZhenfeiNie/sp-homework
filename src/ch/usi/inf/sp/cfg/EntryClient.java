package ch.usi.inf.sp.cfg;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Please use this as the entry of the program.<br>
 * <b>NOTE: This setup-class is copied from Yudi's implementation with some slight changes.</b>
 * @author Yudi Zheng
 * @author Zhenfei Nie <zhen.fei.nie@usi.ch>
 *
 */
public class EntryClient {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		for (String arg : args) {
			System.out.println(arg);
			try {
				RandomAccessFile raf = new RandomAccessFile(arg, "r");
				// read magic number
				int magic = raf.readInt();
				raf.close();

				// test if it is a zip file
				if (magic == 0x504B0304) {
					JarFile jar = new JarFile(arg);
					final Enumeration<JarEntry> entries = jar.entries();

					while (entries.hasMoreElements()) {
						final JarEntry entry = entries.nextElement();
						if (!entry.isDirectory()
								&& entry.getName().endsWith(".class")) {
							analyze(jar.getInputStream(entry));
						}
					}
				} else {
					analyze(new FileInputStream(arg));
				}
			} catch (FileNotFoundException e) {
				System.out.println(arg + " not found!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void analyze(InputStream input) throws Exception {
		final ClassReader cr = new ClassReader(input);
		// create an empty ClassNode (in-memory representation of a class)
		final ClassNode clazz = new ClassNode();
		// have the ClassReader read the class file and populate the ClassNode
		// with the corresponding information
		cr.accept(clazz, 0);
		@SuppressWarnings("unchecked")
		final List<MethodNode> methods = clazz.methods;
		for (MethodNode methodNode : methods) {
			// skipping abstract or native methods
			int access = methodNode.access;
			
			if ((access & Opcodes.ACC_ABSTRACT) != 0
					|| (access & Opcodes.ACC_NATIVE) != 0) {
				continue;
			}
			
			final ControlFlowGraphExtractor extractor = new ControlFlowGraphExtractor();
			ControlFlowGraph cfg = extractor.create(methodNode);
			DominatorAnalysis da = new ZNDominatorAnalysis();
			System.out.println( da.analyse(cfg).generateDot() );
			
		}
	}
	
	
}
