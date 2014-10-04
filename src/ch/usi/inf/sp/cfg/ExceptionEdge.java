package ch.usi.inf.sp.cfg;

/**
 * 
 * @author niezhenfei
 *
 */
public class ExceptionEdge extends Edge {

	public ExceptionEdge(Block start, Block end, String label) {
		super(start, end, label);
	}

	@Override
	public String generateDot() {
		return null;
	}
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof ExceptionEdge ) {
			
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		
		return 0;
	}
}
