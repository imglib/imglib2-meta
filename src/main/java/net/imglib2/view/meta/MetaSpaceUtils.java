package net.imglib2.view.meta;

import java.util.Iterator;

import net.imglib2.RandomAccessible;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.MixedTransformView;

public class MetaSpaceUtils
{
	public static < T > RandomAccessible< T > addNonVaryingDimensions(
			final RandomAccessible< T > data,
			final OrderedAxisSet varyingAxes )
	{
		assert ( data.numDimensions() == varyingAxes.size() );
		final MixedTransform t = new MixedTransform( varyingAxes.numDimensions(), data.numDimensions() );
		t.setComponentMapping( varyingAxes.toArray() );
		return new MixedTransformView<>( data, t );
	}

	public static < T > RandomAccessible< T > wrapAsRandomAccessible(
			final T data,
			final int numDimensions )
	{
		final MixedTransform t = new MixedTransform( numDimensions, 0 );
		return new MixedTransformView<>( new ZeroDimRandomAccessibleInterval<>( data ), t );
	}

	/**
	 * Build an iterator of the values of a {@link MetaSpace}. Works for both
	 * {@link MetaSpaceContainer}s and {@link MetaSpaceView}s. This requires
	 * that the value type of the {@link MetaSpace} implements
	 * {@link HasAttachedAxes}.
	 *
	 * @param space
	 * @return iterator of values in the space.
	 */
	public static < S extends MetaSpace< S, T >, T extends HasAttachedAxes > Iterator< T > iterator( final S space )
	{
		if ( space instanceof MetaSpaceContainer )
			return ( ( MetaSpaceContainer< ?, T > ) space ).elements.values().iterator();
		else if ( space instanceof MetaSpaceView )
		{
			final MetaSpaceView< S, T > view = ( MetaSpaceView< S, T > ) space;
			final Iterator< T > sourceIt = iterator( (S) view.getSource() );
			return new Iterator< T >()
			{
				private final OrderedAxisSet viewAxes;

				private T next;

				{
					viewAxes = new OrderedAxisSet( view.numDimensions() );
					prepareNext();
				}

				private void prepareNext()
				{
					while( sourceIt.hasNext() )
					{
						final T t = sourceIt.next();
						final OrderedAxisSet sourceAxes = t.getAttachedToAxes();
						view.axisMap.applyInverse( viewAxes, sourceAxes );
						if ( viewAxes.size() == sourceAxes.size() )
						{
							next = view.getIfExists( viewAxes );
							return;
						}
					}
					next = null;
				}

				@Override
				public boolean hasNext()
				{
					return next != null;
				}

				@Override
				public T next()
				{
					final T current = next;
					prepareNext();
					return current;
				}
			};
		}
		else
			throw new IllegalArgumentException();
	}
}
