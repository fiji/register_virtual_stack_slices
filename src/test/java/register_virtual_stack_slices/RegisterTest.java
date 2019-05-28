package register_virtual_stack_slices;

import ij.ImageJ;
import register_virtual_stack.Register_Virtual_Stack_MT;

/**
 * Test class for debugging plugin.
 */
public class RegisterTest {

	/**
	 * Main method to test and debug the Register Virtual Stack Slices GUI
	 *
	 * @param args
	 */
	public static void main( final String[] args )
	{
		ImageJ.main( args );

		new Register_Virtual_Stack_MT().run( null );
	}
}
