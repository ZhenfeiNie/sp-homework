package ch.usi.inf.sp.cfg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import ch.usi.inf.sp.cfg.DiGraph;
import ch.usi.inf.sp.cfg.Block;
import ch.usi.inf.sp.cfg.Edge;

public class ZNDominatorAnalysis implements DominatorAnalysis {

	class DiGraphIsNotCFGException extends Exception {
		private static final long serialVersionUID = 6215915712030639613L;
	}

	@Override
	public DiGraph analyse(DiGraph input) throws DiGraphIsNotCFGException {
		if ( ! ( input instanceof ControlFlowGraph ) ) {
			throw new DiGraphIsNotCFGException();
		}
		ControlFlowGraph cfg = (ControlFlowGraph)input;
		
		Map<Block, Set<Block>> dominatorSets = new HashMap<Block, Set<Block>>();
		Set<Block> domOfEntry = new HashSet<Block>();
		domOfEntry.add(cfg.entry);
		dominatorSets.put(cfg.entry, domOfEntry);
		
		// initially set all nodes' dominators 
		for ( final Block b : cfg.blocks ) {
			Set<Block> doms = new HashSet<Block>();
			if ( cfg.isEntry(b) ) {
				continue;
			}
			doms.addAll(cfg.blocks);
			dominatorSets.put(b, doms);
		}
		
		boolean isChanged = true;
		while ( isChanged ) {
			isChanged = false;
			
			for ( final Block b : cfg.blocks ) {
				if ( cfg.isEntry(b) || cfg.isEnd(b) ) {
					continue;
				}
				
				// @1 Get all the predecessors
				Set<Block> predecessors = getImmediatePredecessors(b, cfg);
				Set<Block> newDoms = new HashSet<Block>();
				Set<Block> doms = dominatorSets.get(b);
				
				// @2 let b's doms = [ INTERSECT all the predecessors' doms then UNION b itself ]
				for ( Block pred : predecessors ) {
					newDoms = calcuteIntersetion(newDoms, dominatorSets.get(pred));
				}
				newDoms.add(b);
				
				// @3 if b is changed, let isChanged = true
				isChanged = gotChanged(doms, newDoms);
				dominatorSets.put(b, newDoms);
			}
		}
		
		return buildDominatorTree(dominatorSets, cfg);
	}
	
	/**
	 * Remove all the back-edge.
	 * @param dominatorSets
	 * @param cfg
	 * @return
	 */
	public ControlFlowGraph buildDominatorTree( Map<Block, Set<Block>> dominatorSets, ControlFlowGraph cfg ) {
		ControlFlowGraph dt = cfg.clone();
		System.out.println("+++++++++");
		Iterator<Edge> it = dt.edges.iterator();
		while ( it.hasNext() ) {
			Edge e = it.next();
			Block b = e.end;
			boolean flag = true;
			
			for ( Block dom : dominatorSets.get(b) ) {
				Edge de = new Edge(dom, b, "");
				if ( e == de ) {
					flag = false;
					break;
				}
			}
			if ( flag == true ) {
				it.remove();
			}
		}
		return dt;
	}
	
	/**
	 * Get all the immediate predecessors of Block b in CFG cfg.
	 * Node: the exception edges all omitted.
	 * @param b
	 * @param cfg
	 * @return
	 */
	public Set<Block> getImmediatePredecessors(Block b, ControlFlowGraph cfg) {
		Set<Block> predecessors = new HashSet<Block>();
		for ( Edge e : cfg.edges ) {
			if ( e instanceof ExceptionEdge ) {
				continue;
			}
			if ( e.end == b  ) {
				predecessors.add(e.start);
			}
		}
		return predecessors;
	}
	
	/**
	 * Calculation the intersection of two sets.
	 * @param list1
	 * @param list2
	 * @return
	 */
	public Set<Block> calcuteIntersetion(Set<Block> list1, Set<Block> list2  ) {
		if ( list1.size() == 0 || list2.size() == 0 ) {
			return new HashSet<Block>();
		}
		Set<Block> interstion = new HashSet<Block>();
		for ( Block b : list1 ) {
			if ( list2.contains(b) ) {
				interstion.add(b);
			}
		}
		return interstion;
	}
	
	/**
	 * Judge whether the two sets are different or the same.
	 * @param doms
	 * @param newDoms
	 * @return
	 */
	public boolean gotChanged(Set<Block> doms, Set<Block> newDoms) {
		if ( doms.size() != newDoms.size() ) {
			return true;
		}
		
		for ( Block dom : doms ) {
			if ( ! newDoms.contains(dom) ) {
				return true;
			}
		}
		
		for ( Block dom : newDoms ) {
			if ( ! doms.contains(dom) ) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Placeholder for exception
	 * @return
	 */
	public Set<Edge> getAllExceptionEdge() {
		return null;
	}

}
