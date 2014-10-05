
public class Test2 {

	
	public void bar() {
//		int ghouan = 0;
//		try {
//			ghouan = 3 / ghouan;
//		} catch ( Exception e ) {
//			System.out.println( "catch" );
//		} finally {
//			System.out.println( "finally" );
//		}
		
		try {
			m(new NullPointerException());
		} catch ( Exception e ) {
			System.out.println( "catch" );
		} finally {
			System.out.println( "finally" );
		}
		
	}
	
	
	
	public void m(Exception ex) throws Exception {
        throw ex;
	}
}
