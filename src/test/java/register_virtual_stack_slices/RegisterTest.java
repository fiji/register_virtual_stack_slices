/*-
 * #%L
 * Fiji distribution of ImageJ for the life sciences.
 * %%
 * Copyright (C) 2008 - 2022 Fiji developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
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
