package net.imglib2.view;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.type.numeric.real.DoubleType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MetaViews
{

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

	public static boolean[] axisCollectionToFlags( final int numDimensions, final int[] axes )
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

}
