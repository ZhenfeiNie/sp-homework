
public class TestClass1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
	
//	public int switchMethod2(int i) {
//		int j = 0;
//		switch (i) {
//		case 0: j = 0; break;
//		case 1000: j = 1; break;
//		case 2000: j = 2; break;
//		default: j = -1;
//		}
//		return j;
//	}
	
	public void foo() {  
		int i = 0;
        try {
          tryMethod();
          
          if ( i < 100 ) {
        	  throw new NullPointerException();
          } else if ( i > 100 ) {
        	  throw new Exception();
          }
        } catch ( NullPointerException npe) {
        	
        }
        catch (Exception e) {
          catchMethod();
        }finally{
          finallyMethod();
        }
    }
	
	private void tryMethod() throws Exception{}

    private void catchMethod() throws NullPointerException, NullPointerException {}

    private void finallyMethod(){}
}
