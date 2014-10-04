package ch.usi.inf.sp.cfg;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.util.Printer;

public class Block implements Dottable {
	/**
	 * The index is the first instruction
	 */
	public int index;
	
	public List<AbstractInsnNode> instructions;
	
	/**
	 * line number of every instuctions
	 */
	public List<Integer> indices;
	
	public Block(int index) {
		this.index = index;
		instructions = new LinkedList<AbstractInsnNode>();
		this.indices = new LinkedList<Integer>();
		
	}
	
	/**
	 * 
	 * @param instruction 
	 * @param index - The index of the instruction in the origin instruction list.
	 */
	public void addInstruction( AbstractInsnNode instruction, int index) {
		instructions.add(instruction);
		indices.add(index);
	}
	
	public String getTitle() {
		if ( this.index < 0 ) {
			return this.index == -1 ? "S" : "E";
		}
		return "B" + String.valueOf(this.index);
	}
	
	public AbstractInsnNode getLastInsn() {
		if ( this.instructions.size() == 0 ) {
			return null;
		}
		int i=instructions.size()-1;
		while ( ! Tools.isRealInsn(instructions.get(i)) && i!=0) {
			i--;
		}
		
		if ( i< 0 ) {
			return null;
		}
		return this.instructions.get( i );
	}
	
	@Override
	public String generateDot() {
		StringBuffer sb = new StringBuffer();
		
		sb.append(getTitle() );
		if ( getTitle() == "E" || getTitle() == "S" ) {
			sb.append(" [label=\"" + getTitle());
		} else {
			sb.append(" [shape=record, " + "label=\"" + getTitle() + " | {");
		}
		boolean flag = false;
		for ( int i=0; i<this.instructions.size(); i++ ) {
			String mnemonic = Tools.getMnemonic(this.instructions.get(i));
			if ( mnemonic == null ) {
				continue;
			}
			
			if ( flag == true && i != 0 ) {
				sb.append(" | ");
			}
			sb.append(this.indices.get(i) + " : " + mnemonic);
			flag = true;
		}
		if ( getTitle() != "E" && getTitle() != "S" ) {
			sb.append(" }");
		} 
		sb.append("\"]\n");
		return sb.toString();
	}
	
	public boolean contains( int index ) {
		if ( this.indices != null ) {
			return this.indices.contains(index);
		}
		return false;
	}
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof Block ) {
			Block b = (Block)o;
			if ( this.index == b.index && this.indices.size() == b.indices.size()) {
				for ( int i=0; i<this.indices.size(); i++ ) {
					if ( this.indices.get(i) != b.indices.get(i) ) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		
		return 0;
	}
	
	@Override
	public String toString() {
		return this.getTitle();
	}

}
