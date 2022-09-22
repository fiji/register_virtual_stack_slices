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
package register_virtual_stack;

/** 
 * Albert Cardona, Ignacio Arganda-Carreras and Stephan Saalfeld 2009. 
 * This work released under the terms of the General Public License in its latest edition. 
 * */

import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import mpicbg.trakem2.transform.CoordinateTransform;
import mpicbg.trakem2.transform.CoordinateTransformList;
import mpicbg.trakem2.transform.TransformMesh;
import mpicbg.trakem2.transform.TransformMeshMapping;

/** 
 * Fiji plugin to transform sequences of images in a concurrent (multi-thread) way.
 * <p>
 * <b>Requires</b>: 
 * <ul>
 * 		<li>Source folder: a directory with images, of any size and type (8, 16, 32-bit gray-scale or RGB color)</li>
 * 		<li>Transform folder: a directory with the transform files (from a <a target="_blank" href="http://fiji.sc/wiki/Register_Virtual_Stack_Slices">Register_Virtual_Stack_Slices</a> execution). </li>
 * </ul>
 * <p>
 * <b>Performs</b>: transformation of the sequence of images by applying the transform files.
 * <p>
 * <b>Outputs</b>: the list of new images, one for slice, into a output directory as .tif files.
 * <p>
 * For a detailed documentation, please visit the plugin website at:
 * <p>
 * <A target="_blank" href="http://fiji.sc/wiki/Transform_Virtual_Stack_Slices">http://fiji.sc/wiki/Transform_Virtual_Stack_Slices</A>
 * 
 * @author Ignacio Arganda-Carreras (ignacio.arganda@gmail.com), Stephan Saalfeld and Albert Cardona
 */
public class Transform_Virtual_Stack_MT implements PlugIn 
{		
	/** source directory **/
	public static String sourceDirectory="";
	/** output directory **/
	public static String outputDirectory="";
	/** transforms directory **/
	public static String transformsDirectory="";
	/** interpolate? **/
	public static boolean interpolate=true;
   

	//---------------------------------------------------------------------------------
	/**
	 * Plug-in run method
	 * 
	 * @param arg plug-in arguments
	 */
	public void run(String arg) 
	{
		GenericDialogPlus gd = new GenericDialogPlus("Transform Virtual Stack");

		gd.addDirectoryField("Source directory", sourceDirectory, 50);
		gd.addDirectoryField("Output directory", outputDirectory, 50);
		gd.addDirectoryField("Transforms directory", transformsDirectory, 50);
		gd.addCheckbox( "interpolate", interpolate );
		
		gd.showDialog();
		
		// Exit when canceled
		if (gd.wasCanceled()) 
			return;
		
		sourceDirectory = gd.getNextString();
		outputDirectory = gd.getNextString();
		transformsDirectory = gd.getNextString();
		interpolate = gd.getNextBoolean();
				

		String source_dir = sourceDirectory;
		if (null == source_dir) 
			return;
		source_dir = source_dir.replace('\\', '/');
		if (!source_dir.endsWith("/")) source_dir += "/";
		

		String target_dir = outputDirectory;
		if (null == target_dir) 
			return;
		target_dir = target_dir.replace('\\', '/');
		if (!target_dir.endsWith("/")) target_dir += "/";		

		String transf_dir = transformsDirectory;
		if (null == transf_dir) 
			return;
		transf_dir = transf_dir.replace('\\', '/');
		if (!transf_dir.endsWith("/")) transf_dir += "/";

		// Execute transformation
		exec( source_dir, target_dir, transf_dir, interpolate );

	}

	//---------------------------------------------------------------------------------
	/**
	 * Transform images in the source directory applying transform files from a specific directory.
	 * 
	 * @param source_dir folder with input (source) images.
	 * @param target_dir folder to store output (transformed) images.
	 * @param transf_dir folder with transform files.
	 * @param interpolate boolean flag to interpolate the results or not
	 * @return true for correct execution, false otherwise.
	 */
	public static boolean exec(
			final String source_dir, 
			final String target_dir, 
			final String transf_dir,
			final boolean interpolate) 
	{
		// Get source file listing		
		final String[] src_names = new File(source_dir).list(new FilenameFilter() 
		{
			public boolean accept(File dir, String name) 
			{
				int idot = name.lastIndexOf('.');
				if (-1 == idot) return false;
				return Register_Virtual_Stack_MT.exts.contains( 
						name.substring( idot ).toLowerCase() );
			}
		});
		Arrays.sort(src_names);
		
		// Get transform file listing
		final String ext_xml = ".xml";
		final String[] transf_names = new File(transf_dir).list(new FilenameFilter() 
		{
			public boolean accept(File dir, String name) 
			{
				int idot = name.lastIndexOf('.');
				if (-1 == idot) return false;
				return ext_xml.contains(name.substring(idot).toLowerCase());
			}
		});
		Arrays.sort(transf_names);
		
		// Check the number of input (source) files and transforms.
		if(transf_names.length != src_names.length)
		{
			IJ.error("The number of source and transform files must be equal!");
			return false;
		}
		
		// Read transforms
		CoordinateTransform[] transform = new CoordinateTransform[transf_names.length];
		for(int i = 0; i < transf_names.length; i ++)
		{
			transform[i] = readCoordinateTransform(transf_dir + transf_names[i]);
			if(transform[i] == null)
			{
				IJ.error("Error when reading transform from file: " + transf_dir + transf_names[i]);
				return false;
			}
		}
		
		// Apply transforms
			
		// Create transformed images
		IJ.showStatus("Calculating transformed images...");
		if(Register_Virtual_Stack_MT.createResults(source_dir, src_names, target_dir, null, transform, interpolate) == false)
		{
			IJ.log("Error when creating transformed images");
			return false;
		}
				
		return true;
	}
	
	//---------------------------------------------------------------------------------
	/**
	 * Transform range of images in the source directory applying transform
	 * files from a specific directory.
	 *
	 * @param source_dir folder with input (source) images.
	 * @param target_dir folder to store output (transformed) images.
	 * @param transf_dir folder with transform files.
	 * @param interpolate boolean flag to interpolate the results or not
	 * @return true for correct execution, false otherwise.
	 */
	public static boolean exec(
			final String source_dir,
			final String target_dir,
			final String transf_dir,
			final int first,
			final int last,
			final boolean interpolate )
	{
		// Get source file listing
		String[] src_names = new File(source_dir).list(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				int idot = name.lastIndexOf('.');
				if (-1 == idot) return false;
				return Register_Virtual_Stack_MT.exts.contains(
						name.substring( idot ).toLowerCase() );
			}
		});
		Arrays.sort(src_names);

		// Get transform file listing
		final String ext_xml = ".xml";
		final String[] transf_names = new File( transf_dir ).list(
				new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				int idot = name.lastIndexOf('.');
				if (-1 == idot) return false;
				return ext_xml.contains(name.substring(idot).toLowerCase());
			}
		});
		Arrays.sort(transf_names);

		// Check the number of input (source) files and transforms.
		if(transf_names.length != src_names.length)
		{
			IJ.error("The number of source and transform files must be equal!");
			return false;
		}

		if( first < 0 || first > last || last >= src_names.length )
		{
			IJ.log( "Error: wrong indexes ("+ first + "<->" + last+")" );
			return false;
		}

		// Read transforms
		CoordinateTransform[] transform = new CoordinateTransform[transf_names.length];
		for(int i = 0; i < transf_names.length; i ++)
		{
			transform[i] = readCoordinateTransform(transf_dir + transf_names[i]);
			if(transform[i] == null)
			{
				IJ.error("Error when reading transform from file: "
							+ transf_dir + transf_names[i]);
				return false;
			}
		}

		// Create sub-lists based on the indexes
		src_names = Arrays.copyOfRange( src_names, first, last );
		transform = Arrays.copyOfRange( transform, first, last );

		// Create transformed images
		IJ.showStatus("Calculating transformed images...");
		if( Register_Virtual_Stack_MT.createResults( source_dir, src_names,
				target_dir, null, transform, interpolate ) == false )
		{
			IJ.log("Error when creating transformed images");
			return false;
		}

		return true;
	}


	//---------------------------------------------------------------------------------
	/**
	 * Read coordinate transform from file (generated in Register_Virtual_Stack)
	 *
	 * @param filename  complete file name (including path)
	 * @return true if the coordinate transform was properly read, false otherwise.
	 */
	public static CoordinateTransform readCoordinateTransform( String filename )
	{
		final CoordinateTransformList<CoordinateTransform> ctl = new CoordinateTransformList<CoordinateTransform>();
		try 
		{
			final FileReader fr = new FileReader(filename);
			final BufferedReader br = new BufferedReader(fr);
			String line = null;
			while ((line = br.readLine()) != null) 
			{
				int index = -1;
				if( (index = line.indexOf("class=")) != -1)
				{
					// skip "class"
					index+= 5;
					// read coordinate transform class name
					final int index2 = line.indexOf("\"", index+2); 
					final String ct_class = line.substring(index+2, index2);
					final CoordinateTransform ct = (CoordinateTransform) Class.forName(ct_class).newInstance();
					// read coordinate transform info
					final int index3 = line.indexOf("=", index2+1);
					final int index4 = line.indexOf("\"", index3+2); 
					final String data = line.substring(index3+2, index4);
					ct.init(data);
					ctl.add(ct);
				}
			}
			br.close();
		
		} catch (FileNotFoundException e) {
			IJ.error("File not found exception" + e);
			
		} catch (IOException e) {
			IJ.error("IOException exception" + e);
			
		} catch (NumberFormatException e) {
			IJ.error("Number format exception" + e);
			
		} catch (InstantiationException e) {
			IJ.error("Instantiation exception" + e);
			
		} catch (IllegalAccessException e) {
			IJ.error("Illegal access exception" + e);
			
		} catch (ClassNotFoundException e) {
			IJ.error("Class not found exception" + e);
			
		}
		return ctl;
	}

	/**
	 * Apply a coordinate transform to a given image
	 *
	 * @param imp  image to be transformed
	 * @param transform  transform to be applied
	 * @param meshResolution  number of vertices per row in transform mesh
	 * @param interpolate  flag to interpolate pixel values
	 * @param worldOrigin  image world origin coordinates after transform
	 * @return transformed image
	 */
	public static ImagePlus applyCoordinateTransform(
			final ImagePlus imp,
			final CoordinateTransform transform,
			final int meshResolution,
			final boolean interpolate,
			int[] worldOrigin )
	{
		if( null == imp || null == transform || null == worldOrigin )
		{
			IJ.error("Error: missing parameter! An image, a coordinate"
					+ " transform and a rectangle (to store world info) "
					+ "are needed.");
			return null;
		}
		// Calculate transform mesh
		TransformMesh mesh =
				new TransformMesh( transform, meshResolution,
						imp.getWidth(), imp.getHeight() );
		TransformMeshMapping mapping = new TransformMeshMapping( mesh );

		// Store world origin after transform
		Rectangle worldBounds = mesh.getBoundingBox();
		worldOrigin[ 0 ] = worldBounds.x;
		worldOrigin[ 1 ] = worldBounds.y;

		// Create interpolated deformed image with black background
		imp.getProcessor().setValue( 0 );
		final ImageProcessor ip2 = interpolate ?
				mapping.createMappedImageInterpolated( imp.getProcessor() ) :
				mapping.createMappedImage( imp.getProcessor() );
		final ImagePlus result = new ImagePlus( imp.getTitle(), ip2 );
		result.setCalibration( imp.getCalibration() );
		return result;
	}
	
	
}// end class Register_Virtual_Stack_MT
