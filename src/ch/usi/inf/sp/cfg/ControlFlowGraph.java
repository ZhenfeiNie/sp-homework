package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;

public class ControlFlowGraph implements DiGraph {
	public String name;
	
	public List<Block> blocks;
	public List<Edge> edges;
	
	public Block entry;
	public Block end;
	
	public ControlFlowGraph(String name) {
		this.name = name;
		
		this.blocks = new ArrayList<Block>();
		this.edges = new ArrayList<Edge>();
		
		
	}
	
	public void addEntry( ) {
		this.entry = new Block(-1);
		this.blocks.add(entry);
	}
	
	public void addEnd() {
		this.end = new Block(-2);
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
	
	public boolean isEntry(Block b) {
		return this.entry == b;
	}
	
	public boolean isEnd(Block b) {
		return this.end == b;
	}
	
	@Override
	public ControlFlowGraph clone() {
		ControlFlowGraph newCfg = new ControlFlowGraph(this.name);
		
		for ( int i=0; i<this.blocks.size(); i++ ) {
			Block b = this.blocks.get(i);
			newCfg.addBlock(b.clone());
		}
		
		for ( Edge e : this.edges ) {
			newCfg.addEdge(e.clone());
		}
		
		newCfg.entry = this.entry;
		newCfg.end = this.end;
		
		return newCfg;
	}
	
}
