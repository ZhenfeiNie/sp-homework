
public class Test2 {

	
	public void bar() throws Exception{
//		int ghouan = 0;
//		try {
//			ghouan = 3 / ghouan;
//		} catch ( Exception e ) {
//			System.out.println( "catch" );
//		} finally {
//			System.out.println( "finally" );
//		}
		
		try {
			m(new Exception());
		} catch ( NullPointerException e ) {
			System.out.println( "catch" );
		} finally {
			System.out.println( "finally" );
		}
		
	}
	
	
	
	public void m(Exception ex) throws Exception {
        throw ex;
	}
	
	
	public static void main (String args[]) throws Exception {
		Test2 t = new Test2();
		t.bar();
		System.out.println();
	}
}
