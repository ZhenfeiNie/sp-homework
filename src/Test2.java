/**
 * Just a normal class that is used to test the exception problems. There is a method 
 * that is adopt from Yudi Zheng's Example.class's throwExceptionNested().
 * @author Zhenfei Nie <zhen.fei.nie@usi.ch>
 *
 */
public class Test2 {
	public void bar() throws Exception{
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
	
	public static void main (String args[]) {

		Test2 t = new Test2();
		t.throwExceptionNested();
		System.out.println();
	}
	
	/**
	 * Adopt from Yudi Zheng's Example.class's throwExceptionNested().
	 */
	public void throwExceptionNested() {
		try {
			System.out.println("try");
			throw new Exception();
		} catch (NullPointerException e) {
			System.out.println("catch NullPointerException");
			throw e;
		} catch (Exception e) {
			System.out.println("catch Exception");
			throw new RuntimeException();
		} finally {
			System.out.println("finally");
		}
	}
	
}
