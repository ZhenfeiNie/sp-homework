package ch.usi.inf.sp.cfg;

public class Edge implements Dottable {

	public Block start;
	public Block end;
	public String label;
	
	public Edge ( Block start, Block end, String label ) {
		this.start = start;
		this.end = end;
		this.label = label;
	}
	
	@Override
	public String generateDot() {
		
		StringBuffer sb = new StringBuffer();
		sb.append(start + " -> " + end);
		 
		if ( ! label.isEmpty() ) {
			sb.append("[label=\"");
			sb.append(this.label);
			sb.append("\"]");
		}
		sb.append("\n");		
		
		
		
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof Edge ) {
			Edge e = (Edge)o;
			return this.start == e.start ? 
					( this.end == e.end ? this.label == e.label : false) : false;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		
		return 0;
	}

}
