package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ControlFlowGraph implements Dottable {
	public String name;
	
	public List<Block> blocks;
	public List<Edge> edges;
	
	public Block entry;
	public Block end;
	
	public ControlFlowGraph(String name) {
		this.name = name;
		
		this.blocks = new ArrayList<Block>();
		this.edges = new ArrayList<Edge>();
		
		this.entry = new Block(-1);
		this.end = new Block(-2);
		
		this.blocks.add(entry);
		this.blocks.add(end);
	}
	
	public void addBlock(Block b) {
		this.blocks.add(b);
	}
	
	public void addEdge(Edge e) {
		this.edges.add(e);
	}
	
	@Override
	public String generateDot() {
		StringBuffer sb = new StringBuffer();
		
		for ( Block b : this.blocks ) {
			sb.append( b.generateDot() );
		}
		
		for ( Edge e : this.edges ) {
			sb.append( e.generateDot() );
		}
		
		return sb.toString();
	}

	
	public Block findBlockByInsn( int index ) {
		for ( Block b : this.blocks ) {
			if ( b.contains(index) ) {
				return b;
			}
		}
		return null;
	}
}
