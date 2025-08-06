package net.imglib2.meta;

import net.imglib2.*;

/**
 * A piece of m-dimensional metadata associated with a n-dimensional dataset (n&ge;m).
 * <p>
 * Pertains to dimensions {d<sub>1</sub>, d<sub>2</sub>, ..., d<sub>m</sub>},
 * where d<sub>i</sub> is within [0, n-1].
 * </p>
 * @param <T> the element type
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public interface MetadataItem<T> extends RandomAccessible<T> {

	/**
	 * Returns the key identifying this piece of metadata
	 * @return the key
	 */
	String name();

	boolean[] attachedAxes();

	/**
	 * Returns the value of the metadata in {@code n}-dimensional space.
	 *
	 * @param pos - a point in {@code n} dimensions
	 * @return the value of the metadata at {@code pos}
	 * @see #getAt(RealLocalizable) to get an object queryable in m-dimensional space.
	 */
	T getAt(RealLocalizable pos);

	// -- default utility methods -- //

	/**
	 * Describes whether this {@link MetadataItem} is attached to <em>any</em> dimensions.
	 * @return {@code true} iff this {@link MetadataItem} pertains to any dimension.
	 */
	default boolean isAttachedToAnyAxis() {
		for(boolean b: attachedAxes()) if (b) return true;
		return false;
	}

	/**
	 * Describes whether all dimensions in {@code dims} are in {d<sub>1</sub>, d<sub>2</sub>, ..., d<sub>m</sub>}.
	 * @param dims a list of dimensional indices
	 * @return {@code true} iff this {@link MetadataItem} pertains to all
	 * 		dimensional indices in {@code dims}.
	 */
	default boolean isAttachedTo(final int... dims) {
		boolean[] attachedAxes = attachedAxes();
		for(int i: dims) {
			// Cannot be attached to a dimension beyond the data space.
			if (i < 0 || attachedAxes.length <= i) {
				return false;
			}
			if (!attachedAxes[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the value of the metadata in {@code n}-dimensional space.
	 *
	 * @param pos - a length-{@code n} array of dimensional coordinates
	 * @return the value of the metadata at {@code pos}
	 * @see #get() to get an object queryable in m-dimensional space.
	 */
	default T getAt(long... pos) {
		return getAt(new Point(pos));
	}

	/**
	 * Returns the value of the metadata in {@code n}-dimensional space.
	 *
	 * @param pos - a length-{@code n} array of dimensional coordinates
	 * @return the value of the metadata at {@code pos}
	 * @see #get() to get an object queryable in m-dimensional space.
	 */
	default T getAt(double... pos) {
		return getAt(new RealPoint(pos));
	}

	// -- RandomAccessible Overrides -- //

	default int numDimensions() {
		int ndim = 0;
		for (boolean b : attachedAxes()) if (b) ndim++;
		return ndim;
	}

	default T getType() {
        return getAt(new long[numDimensions()]);
	}

	default RandomAccess<T> randomAccess(Interval interval) {
		return randomAccess();
	}
}
