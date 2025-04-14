package net.imglib2.view;

import java.util.*;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Localizables;
import net.imglib2.util.Util;
import net.imglib2.view.meta.InvertibleAxisMap;
import net.imglib2.view.meta.MixedTransforms;
import net.imglib2.view.meta.OrderedAxisSet;

public class MetaViews
{

	public static void main( final String[] args )
	{
		final int numDimensions = 5; // XYZCT

		final ArrayList< MetaDataItem< ? > > metadata = new ArrayList<>();

		metadata.add(
				new SimpleItem<>( "X Calib", "2 um",
						axisCollectionToFlags( numDimensions, new int[] { 0 } ) ) );
		metadata.add(
				new SimpleItem<>( "Y Calib", "5 um",
						axisCollectionToFlags( numDimensions, new int[] { 1 } ) ) );
		metadata.add(
				new SimpleItem<>( "Z Calib", "10 um",
						axisCollectionToFlags( numDimensions, new int[] { 2 } ) ) );
		metadata.add(
				new SimpleItem<>( "Spatial", "isSpatial",
						axisCollectionToFlags( numDimensions, new int[] { 0, 1, 2 } ) ) );

		final int[] numbersXY = new int[] {
				0, 1,
				2, 3 };
		final VaryingItem< IntType > numbersXYItem = new VaryingItem<>( "Numbers [XY]", ArrayImgs.ints( numbersXY, 2, 2 ),
				axisCollectionToFlags( numDimensions, new int[] { 0, 1 } ) );
		metadata.add( numbersXYItem );


		System.out.println( "XYZCT" );
		for ( final MetaDataItem< ? > item : metadata )
			System.out.println( item );


		System.out.println();
		System.out.println( "ZYXCT rotate" );
		for ( final MetaDataItem< ? > item : metadata )
			System.out.println( rotate( item, 0, 2 ) );


		System.out.println();
		System.out.println( "XYTCZ permute" );
		for ( final MetaDataItem< ? > item : metadata )
			System.out.println( permute( item, 2, 4 ) );


		System.out.println();
		System.out.println( "XYTC permute hyperSlice" );
		for ( final MetaDataItem< ? > item : metadata )
			System.out.println( hyperSlice( permute( item, 2, 4 ), 4, 0 ) );


		System.out.println();
		System.out.println( "--------------------" );
		System.out.println();


		IntervalIterator i;
		i = new IntervalIterator( new int[] { 2, 2, 2, 2, 2 } );
		while( i.hasNext() )
		{
			i.fwd();
			final int[] coords = new int[ i.numDimensions() ];
			i.localize( coords );
			System.out.println( Util.printCoordinates( coords ) + " -> " + numbersXYItem.getAt( i ) );
		}

		System.out.println();
		System.out.println();

		System.out.println( "XCZTY permute" );
		i = new IntervalIterator( new int[] { 2, 2, 2, 2, 2 } );
		while( i.hasNext() )
		{
			i.fwd();
			final int[] coords = new int[ i.numDimensions() ];
			i.localize( coords );
			System.out.println( Util.printCoordinates( coords ) + " -> " + permute( numbersXYItem, 1, 4 ).getAt( i ) );
		}

		System.out.println();
		System.out.println();

		System.out.println( "CZTY permute hyperslice" );
		i = new IntervalIterator( new int[] { 2, 2, 2, 2 } );
		while( i.hasNext() )
		{
			i.fwd();
			final int[] coords = new int[ i.numDimensions() ];
			i.localize( coords );
			System.out.println( Util.printCoordinates( coords ) + " -> " +
					hyperSlice( permute( numbersXYItem, 1, 4 ), 0, 1 ).getAt( i ) );
		}
	}

	// TODO: Make this an interface
	public static class MetaData {

		private List<MetaDataItem<?>> items;
		private MixedTransformView<?> view;

		public MetaData() {
			this(new ArrayList<>(), null);
		}

		public MetaData(List<MetaDataItem<?>> items, MixedTransformView view) {
			this.items = items;
			this.view = view;
		}

		public List<MetaDataItem<?>> items() {
			return items;
		}

		/**
		 *
		 * @param key the metadata key
		 */
		public Optional<MetaDataItem<?>> get(String key) {
			return items.stream() //
					.filter(item -> item.name().equals(key))
					.filter(item -> !item.isAttachedToAxes())
					.findFirst();
		}

		/**
		 *
		 * @param key the metadata key
		 * @param d the axis
		 */
		public Optional<MetaDataItem<?>> get(String key, int d) {
			final int dd = view == null ? d : view.getTransformToSource().getComponentMapping(d);
			return items.stream() //
					.filter(item -> item.name().equals(key))
					.filter(item -> item.isAttachedTo(dd)) //
					.map(item -> view == null ? item : item.view(view))
					.findFirst();
		}

		public MetaData view(MixedTransformView<DoubleType> v) {
			// TODO: Can we chain them? That'd be a cool trick
			if(this.view != null)
				throw new UnsupportedOperationException("You must call view() on the original MetaData");
			return new MetaData(this.items, v);
		}
	}





	public static interface MetaDataItem< T >
	{

		public String name();
		public boolean isAttachedToAxes();
		public boolean isAttachedTo(final int d);
		public T get();
		public T getAt( Localizable pos );
		default T getAt( long... pos ) {
			return getAt(new Point(pos));
		}

		MetaDataItem<?> view(MixedTransformView<?> view);
	}

	public static class SimpleItem< T > implements MetaDataItem< T >
	{
		final String name;

		final T data;

		final boolean[] attachedToAxes;

		public SimpleItem( final String name, final T data )
		{
			this( name, data, null );
		}

		public SimpleItem( final String name, final T data, final boolean[] attachedToAxes )
		{
			this.name = name;
			this.data = data;
			this.attachedToAxes = attachedToAxes;
		}

		public boolean isAttachedToAxes()
		{
			return attachedToAxes != null;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public boolean isAttachedTo(int d) {
			return attachedToAxes != null && attachedToAxes[d];
		}

		@Override
		public T get() {
			return data;
		}

		@Override
		public T getAt( final Localizable pos )
		{
			return get();
		}

		@Override
		public MetaDataItem<?> view(MixedTransformView<?> view) {
			return this;
		}

		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder( "SimpleItem \"" );
			sb.append( name );
			sb.append( "\"; " );

			if ( isAttachedToAxes() )
			{
				sb.append( "attached to axes {" );
				final int[] axes = flagsToAxisList( attachedToAxes );
				sb.append( axes[ 0 ] );
				for ( int i = 1; i < axes.length; ++i )
					sb.append( ", " + axes[i] );
				sb.append( "}; " );
			}
			else
				sb.append( "not attached to any axis; " );

			sb.append( "value = " + data );

			return sb.toString();
		}
	}

	public static class VaryingItem< T > implements MetaDataItem< T >
	{
		final String name;

		final RandomAccessible< T > data;
		final MixedTransformView<?> view;

		final boolean[] variesWithAxes;

		final boolean[] attachedToAxes;

		public VaryingItem( final String name, final RandomAccessible< T > data, final boolean[] variesWithAxes )
		{
			this( name, data, variesWithAxes, null );
		}

		public VaryingItem( final String name, final RandomAccessible< T > data, final boolean[] variesWithAxes, final boolean[] attachedToAxes )
		{
			this(name, data, null, variesWithAxes, attachedToAxes);
		}

		private VaryingItem( final String name, final RandomAccessible< T > data, final MixedTransformView<?> view, final boolean[] variesWithAxes, final boolean[] attachedToAxes )
		{
			this.name = name;
			this.data = data;
			this.view = view;
			this.variesWithAxes = variesWithAxes;
			this.attachedToAxes = attachedToAxes;
		}

		public boolean isAttachedToAxes()
		{
			return attachedToAxes != null;
		}

		@Override
		public T getAt( final Localizable pos )
		{
			Localizable src;
			if (view != null) {
				// FIXME: Yuckkkkkkk
				final MixedTransform tform = view.getTransformToSource();
				final Point dest = new Point(tform.numSourceDimensions());
				view.getTransformToSource().apply(pos, dest);
				src = dest;
			}
			else {
				src = pos;
			}
			final RandomAccess< T > access = data.randomAccess();
			for ( int d = 0, i = 0; d < variesWithAxes.length; ++d )
				if ( variesWithAxes[ d ] )
					access.setPosition( src.getLongPosition( d ), i++ );
			return access.get();
		}

		@Override
		public MetaDataItem<?> view(MixedTransformView<?> view) {
			return new VaryingItem<>(
					this.name,
					this.data,
					view,
					this.variesWithAxes,
					this.attachedToAxes
			);
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public boolean isAttachedTo(int d) {
			return attachedToAxes != null && attachedToAxes[d];
		}

		@Override
		public T get() {
			throw new UnsupportedOperationException("Varying item does not support get()");
		}

		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder( "VaryingItem \"" );
			sb.append( name );
			sb.append( "\"; " );

			if ( isAttachedToAxes() )
			{
				sb.append( "attached to axes {" );
				final int[] axes = flagsToAxisList( attachedToAxes );
				sb.append( axes[ 0 ] );
				for ( int i = 1; i < axes.length; ++i )
					sb.append( ", " + axes[i] );
				sb.append( "}; " );
			}
			else
				sb.append( "not attached to any axis; " );

			return sb.toString();
		}

	}



//	public static < T > MixedTransformView< T > rotate( final RandomAccessible< T > randomAccessible, final int fromAxis, final int toAxis )
//	public static < T > IntervalView< T > rotate( final RandomAccessibleInterval< T > interval, final int fromAxis, final int toAxis )


	public static < T > MetaDataItem< T > rotate( final MetaDataItem< T > item, final int fromAxis, final int toAxis )
	{
		if ( fromAxis == toAxis )
			return item;
		final int numSourceDimensions = getNumSourceDimensions( item );
		if ( numSourceDimensions < 0 )
			return item;
		return mixedTransform( item, MixedTransforms.getRotationTransform( fromAxis, toAxis, numSourceDimensions ) );
	}


	public static < T > MetaDataItem< T > permute( final MetaDataItem< T > item, final int fromAxis, final int toAxis )
	{
		if ( fromAxis == toAxis )
			return item;
		final int numSourceDimensions = getNumSourceDimensions( item );
		if ( numSourceDimensions < 0 )
			return item;
		return mixedTransform( item, MixedTransforms.getPermuteTransform( fromAxis, toAxis, numSourceDimensions ) );
	}


	public static < T > MetaDataItem< T > hyperSlice( final MetaDataItem< T > item, final int d, final long pos )
	{
		final int numSourceDimensions = getNumSourceDimensions( item );
		if ( numSourceDimensions < 0 )
			return item;
		return mixedTransform( item, MixedTransforms.getHyperSliceTransform( d, pos, numSourceDimensions ) );
	}


	/*
	 * Actual work is done here...
	 */

	public static < T > MetaDataItem< T > mixedTransform( final MetaDataItem< T > item, final Mixed transformToSource )
	{
		if ( item instanceof SimpleItem )
			return mix( ( SimpleItem< T > ) item, transformToSource );
		else if ( item instanceof VaryingItem )
			return mix( ( VaryingItem< T > ) item, transformToSource );
		else
			throw new IllegalArgumentException();
	}

	private static < T > SimpleItem< T > mix( final SimpleItem< T > item, final Mixed transformToSource )
	{
		if ( !item.isAttachedToAxes() )
			return item;

		final int n = transformToSource.numSourceDimensions();
		final int m = transformToSource.numTargetDimensions();

		final OrderedAxisSet sourceAxisSet = new OrderedAxisSet( m );
		sourceAxisSet.setPosition( item.attachedToAxes );

		final InvertibleAxisMap bimap = MixedTransforms.getAxisMap( transformToSource );
		final OrderedAxisSet axisSet = bimap.transformToSource( sourceAxisSet );

		final boolean remove = axisSet.size() != sourceAxisSet.size();
		final boolean[] attachedToAxes = new boolean[ n ];
		axisSet.localize( attachedToAxes );

		if ( remove )
			// not attached to any axis anymore
			// remove item ...
			return null; // ???
		else
			return new SimpleItem< T >( item.name, item.data, attachedToAxes );
	}

	private static < T > MetaDataItem< T > mix( final VaryingItem< T > item, final Mixed transformToSource )
	{
		final int n = transformToSource.numSourceDimensions();
		final int m = transformToSource.numTargetDimensions();

		final boolean[] attachedToAxes;
		if ( item.isAttachedToAxes() )
		{
			final OrderedAxisSet sourceAxisSet = new OrderedAxisSet( m );
			sourceAxisSet.setPosition( item.attachedToAxes );

			final InvertibleAxisMap bimap = MixedTransforms.getAxisMap( transformToSource );
			final OrderedAxisSet axisSet = bimap.transformToSource( sourceAxisSet );

			final boolean remove = axisSet.size() != sourceAxisSet.size();
			attachedToAxes = new boolean[ n ];
			axisSet.localize( attachedToAxes );

			if ( remove )
				// not attached to any axis anymore
				// remove item ...
				return null; // ???
		}
		else
			attachedToAxes = null;

		final boolean[] variesWithAxes = new boolean[ n ];
		int numSourceDimensions = 0; // for MixedTransformView of the item.data RandomAccessible
		for ( int d = 0; d < m; ++d )
		{
			if ( item.variesWithAxes[ d ] && !transformToSource.getComponentZero( d ) )
			{
				variesWithAxes[ transformToSource.getComponentMapping( d ) ] = true;
				++numSourceDimensions;
			}
		}

		if ( numSourceDimensions == 0 )
		{
			// last dependent axis sliced away
			// create new SimpleItem< T > with the T

			final Point p = new Point( n );
			final Point q = new Point( m );
			transformToSource.apply( p, q );
			final T data = item.getAt( q );
			return new SimpleItem< T >( item.name, data, attachedToAxes );
		}

		final int[] sourceDimMap = new int[ n ];
		for ( int i = 0, d = 0; d < n; ++d )
			if ( variesWithAxes[ d ] )
				sourceDimMap[ d ] = i++;

		final int numTargetDimensions = item.data.numDimensions();
		final long[] translation = new long[ numTargetDimensions ];
		final boolean[] zero = new boolean[ numTargetDimensions ];
		final boolean[] invert = new boolean[ numTargetDimensions ];
		final int[] component = new int[ numTargetDimensions ];
		for ( int i = 0, d = 0; d < m; ++d )
		{
			if ( item.variesWithAxes[ d ] )
			{
				translation[ i ] = transformToSource.getTranslation( d );
				zero[ i ] = transformToSource.getComponentZero( d );
				invert[ i ] = transformToSource.getComponentInversion( d );
				component[ i ] = sourceDimMap[ transformToSource.getComponentMapping( d ) ];
				++i;
			}
		}

		final MixedTransform dataTransform = new MixedTransform( numTargetDimensions, numTargetDimensions );
		dataTransform.setTranslation( translation );
		dataTransform.setComponentZero( zero );
		dataTransform.setComponentInversion( invert );
		dataTransform.setComponentMapping( component );

		final MixedTransformView< T > transformedData = new MixedTransformView<>( item.data, dataTransform );

		return new VaryingItem<>( item.name, transformedData, variesWithAxes, attachedToAxes );
	}

	private static int getNumSourceDimensions( final MetaDataItem< ? > item )
	{
		if ( item instanceof SimpleItem )
		{
			final SimpleItem< ? > si = ( SimpleItem< ? > ) item;
			if ( si.isAttachedToAxes() )
				return si.attachedToAxes.length;
		}
		else if ( item instanceof VaryingItem )
		{
			final VaryingItem< ? > vi = ( VaryingItem< ? > ) item;
			return vi.variesWithAxes.length;
		}
		else
			throw new IllegalArgumentException();
		return -1;
	}

	/*
	 * axis list helpers...
	 */

	public static boolean[] axisCollectionToFlags( final int numDimensions, final int[] axes )
	{
		final boolean[] flags = new boolean[ numDimensions ];
		for ( final int d : axes )
			flags[ d ] = true;
		return flags;
	}

	public static boolean[] axisCollectionToFlags( final int numDimensions, final Collection< Integer > axes )
	{
		final boolean[] flags = new boolean[ numDimensions ];
		for ( final int d : axes )
			flags[ d ] = true;
		return flags;
	}

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

//	public static < T, F extends EuclideanSpace > RealRandomAccessible< T > interpolate( final F source, final InterpolatorFactory< T, F > factory )
//	public static < T > RandomAccessibleOnRealRandomAccessible< T > raster( final RealRandomAccessible< T > source )
//	public static < T, F extends RandomAccessibleInterval< T > > ExtendedRandomAccessibleInterval< T, F > extend( final F source, final OutOfBoundsFactory< T, ? super F > factory )
//	public static < T, F extends RandomAccessibleInterval< T > > ExtendedRandomAccessibleInterval< T, F > extendMirrorSingle( final F source )
//	public static < T, F extends RandomAccessibleInterval< T > > ExtendedRandomAccessibleInterval< T, F > extendMirrorDouble( final F source )
//	public static < T extends Type< T >, F extends RandomAccessibleInterval< T > > ExtendedRandomAccessibleInterval< T, F > extendValue( final F source, final T value )
//	public static < T extends NumericType< T >, F extends RandomAccessibleInterval< T > > ExtendedRandomAccessibleInterval< T, F > extendZero( final F source )
//	public static < T extends RealType< T >, F extends RandomAccessibleInterval< T > > ExtendedRandomAccessibleInterval< T, F > extendRandom( final F source, final double min, final double max )
//	public static < T, F extends RandomAccessibleInterval< T > > ExtendedRandomAccessibleInterval< T, F > extendPeriodic( final F source )
//	public static < T, F extends RandomAccessibleInterval< T > > ExtendedRandomAccessibleInterval< T, F > extendBorder( final F source )
//	public static < T > IntervalView< T > interval( final RandomAccessible< T > randomAccessible, final long[] min, final long[] max )
//	public static < T > IntervalView< T > interval( final RandomAccessible< T > randomAccessible, final Interval interval )
//	public static < T > MixedTransformView< T > rotate( final RandomAccessible< T > randomAccessible, final int fromAxis, final int toAxis )
//	public static < T > IntervalView< T > rotate( final RandomAccessibleInterval< T > interval, final int fromAxis, final int toAxis )
//	public static < T > MixedTransformView< T > permute( final RandomAccessible< T > randomAccessible, final int fromAxis, final int toAxis )
//	public static < T > IntervalView< T > permute( final RandomAccessibleInterval< T > interval, final int fromAxis, final int toAxis )
//	public static < T > MixedTransformView< T > translate( final RandomAccessible< T > randomAccessible, final long... translation )
//	public static < T > IntervalView< T > translate( final RandomAccessibleInterval< T > interval, final long... translation )
//	public static < T > MixedTransformView< T > offset( final RandomAccessible< T > randomAccessible, final long... offset )
//	public static < T > IntervalView< T > offset( final RandomAccessibleInterval< T > interval, final long... offset )
//	public static < T > IntervalView< T > zeroMin( final RandomAccessibleInterval< T > interval )
//	public static < T > MixedTransformView< T > hyperSlice( final RandomAccessible< T > view, final int d, final long pos )
//	public static < T > IntervalView< T > hyperSlice( final RandomAccessibleInterval< T > view, final int d, final long pos )
//	public static < T > MixedTransformView< T > addDimension( final RandomAccessible< T > randomAccessible )
//	public static < T > IntervalView< T > addDimension( final RandomAccessibleInterval< T > interval, final long minOfNewDim, final long maxOfNewDim )
//	public static < T > MixedTransformView< T > invertAxis( final RandomAccessible< T > randomAccessible, final int d )
//	public static < T > IntervalView< T > invertAxis( final RandomAccessibleInterval< T > interval, final int d )
//	public static < T > IntervalView< T > offsetInterval( final RandomAccessible< T > randomAccessible, final long[] offset, final long[] dimension )
//	public static < T > IntervalView< T > offsetInterval( final RandomAccessible< T > randomAccessible, final Interval interval )
//	public static boolean isZeroMin( final Interval interval )
//	public static < T > IterableInterval< T > iterable( final RandomAccessibleInterval< T > randomAccessibleInterval )
//	public static < T > IterableInterval< T > flatIterable( final RandomAccessibleInterval< T > randomAccessibleInterval )
//	public static < T > CompositeIntervalView< T, ? extends GenericComposite< T > > collapse( final RandomAccessibleInterval< T > source )
//	public static < T extends RealType< T > > CompositeIntervalView< T, RealComposite< T > > collapseReal( final RandomAccessibleInterval< T > source )
//	public static < T extends NumericType< T > > CompositeIntervalView< T, NumericComposite< T > > collapseNumeric( final RandomAccessibleInterval< T > source )
//	public static < T > CompositeView< T, ? extends GenericComposite< T > > collapse( final RandomAccessible< T > source )
//	public static < T extends RealType< T > > CompositeView< T, RealComposite< T > > collapseReal( final RandomAccessible< T > source, final int numChannels )
//	public static < T extends NumericType< T > > CompositeView< T, NumericComposite< T > > collapseNumeric( final RandomAccessible< T > source, final int numChannels )
//	public static < T > SubsampleIntervalView< T > subsample( final RandomAccessibleInterval< T > source, final long step )
//	public static < T > SubsampleIntervalView< T > subsample( final RandomAccessibleInterval< T > source, final long... steps )
//	public static < T > SubsampleView< T > subsample( final RandomAccessible< T > source, final long step )
//	public static < T > SubsampleView< T > subsample( final RandomAccessible< T > source, final long... steps )
//	public static < T > RandomAccessibleInterval< T > dropSingletonDimensions( final RandomAccessibleInterval< T > source )
//	public static < T > RandomAccessibleInterval< T > stack( final List< RandomAccessibleInterval< T > > hyperslices )
//	public static < T > RandomAccessibleInterval< T > stack( final RandomAccessibleInterval< T >... hyperslices )
//	public static < T > RandomAccessibleInterval< T > stack( final StackAccessMode stackAccessMode, final List< RandomAccessibleInterval< T > > hyperslices )
//	public static < T > RandomAccessibleInterval< T > stack( final StackAccessMode stackAccessMode, final RandomAccessibleInterval< T >... hyperslices )
//	public static < T > TransformView< T > shear(
//	public static < T > TransformView< T > unshear(
//	public static < T > IntervalView< T > shear(
//	public static < T > IntervalView< T > unshear(
//	public static < T > IntervalView< T > permuteCoordinates(
//	public static < T > IntervalView< T > permuteCoordinates(
//	public static < T > IntervalView< T > permuteCoordinatesInverse(
//	public static < T > IntervalView< T > permuteCoordinateInverse(
//	public static < A, B > RandomAccessible< Pair< A, B > > pair( final RandomAccessible< A > sourceA, final RandomAccessible< B > sourceB )

}
