package ch.usi.inf.sp.cfg;

import java.util.ArrayList;
import java.util.List;

/**
 * The exception table of a method.
 * @author Zhenfei Nie <zhen.fei.nie@usi.ch>
 *
 */
public final class ExceptionTable {
	public final class ExceptionEntry {
		final int start;
		final int end;
		final int handler;
		String type;
		
		public ExceptionEntry(int start, int end, int handler, String type) {
			super();
			this.start = start;
			this.end = end;
			this.handler = handler;
			this.type = type;
		}
		
		/**
		 * There is chance to optimize this method. 
		 * @see http://boole.inf.usi.ch/sp-2014/assignments/A05.html 
		 * @param location
		 * @param type
		 * @return
		 */
		public int search(final int location, String type) {
			if ( type.endsWith("Error") ) {
				return -2; // -2 means the ex is not caught or it is an error
			} else if ( location >= start && location < end ) {
				if ( this.type == null ) {
					return this.handler;
				} else if ( this.type.equals("java/lang/Exception") ) {
					return this.handler;
				} else if (  this.type.equals(type) || 
							 this.type.endsWith(type) ) {
					return this.handler;
				} else {
					return -2;
				}
			}
			return -2;
		}
		@Override
		public String toString() {
			return "start: " + start + "\n" + 
					"end: " + end + "\n" +
					"handler: " + handler + "\n" + 
					"type: " + type + "\n";
		}
	}
	
	public List<ExceptionEntry> exceptionEntries;
	
	public ExceptionTable() {
		exceptionEntries = new ArrayList<ExceptionEntry>();
	}
	
	public void add(int start, int end, int handler, String type) {
		exceptionEntries.add(new ExceptionEntry(start, end, handler, type));
	}
	
	public int search(final int location, List<String> types) {
		return -1;
	}
	
	public int search(final int location, String type) {
		for ( int i=0; i<this.exceptionEntries.size(); i++ ) {
			ExceptionEntry e = this.exceptionEntries.get(i);
			int handler = e.search(location, type);
			if ( handler != -2) {
				return handler;
			}
		}
		return -2;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for ( ExceptionEntry ee : this.exceptionEntries ) {
			sb.append(ee);
			sb.append("\n");
		}
		return sb.toString();
	}
}
