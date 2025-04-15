package net.imglib2.meta;

import java.util.Arrays;

public class MetaViews
{

	public static int[] flagsToAxisList( final boolean[] flags )
	{
		final int[] tmp = new int[ flags.length ];
		int i = 0;
		for ( int d = 0; d < flags.length; ++d )
			if ( flags[ d ] )
				tmp[ i++ ] = d;
		return Arrays.copyOfRange( tmp, 0, i );
	}

	/*
	 * Types of metadata:
	 *
	 * 1.) simple item attached to one axis
	 *
	 * Example: XYZ calibration in a XYZTC dataset. Represented as 3 attributes.
	 * X calibration attached to X axis, etc.
	 *
	 * 2.) simple item attached to several axes
	 *
	 * Example: attribute "spatial" attached to X,Y,Z axes in XYZTC dataset.
	 *
	 * 3.) simple item not attached to any axis
	 *
	 * Example: color-table
	 *
	 * 4.) item varying with one or more axes
	 *
	 * Example: per-slice color-table
	 *
	 *
	 *
	 *
	 *
	 * varies with axes: indices : int[] data : RandomAccessible< T >
	 * metadata.numDimensions() == indices.length OR does not vary with any
	 * axis: data : T
	 *
	 * attached to axes: indices : int[]
	 *
	 *
	 *
	 *
	 * What about Views.extend()??? RichViews can deal with border, mirror, and
	 * periodic automatically. But probably not random, zero, and value.
	 *
	 *
	 */

}
