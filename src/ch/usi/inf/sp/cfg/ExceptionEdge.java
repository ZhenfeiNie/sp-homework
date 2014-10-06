package ch.usi.inf.sp.cfg;

/**
 * 
 * @author Zhenfei Nie <zhen.fei.nie@usi.ch>
 *
 */
public class ExceptionEdge extends Edge {

	public ExceptionEdge(Block start, Block end, String label) {
		super(start, end, label);
	}

	@Override
	public String generateDot() {
		StringBuffer sb = new StringBuffer();
		sb.append(start + " -> " + end);
		sb.append(" [style=\"dotted\"");
		if ( ! label.isEmpty() ) {
			sb.append(", label=\"");
			sb.append(this.label);
			sb.append("\"");
		}
		sb.append("]");
		sb.append("\n");		
		return sb.toString();
	}
	
}
