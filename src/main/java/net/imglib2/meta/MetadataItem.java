package net.imglib2.meta;

import net.imglib2.Point;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;

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
public interface MetadataItem<T> {

	/**
	 * Returns the key identifying this piece of metadata
	 * @return the key
	 */
	String name();

	default boolean isAttachedToAxes() { return false; }

	/**
	 * Describes whether all dimensions in {@code dims} are in {d<sub>1</sub>, d<sub>2</sub>, ..., d<sub>m</sub>}.
	 * @param dims a list of dimensional indices, where {@code dims}<sub>i</sub>
	 *                is in [0, n-1]
	 * @return {@code true} iff this {@link MetadataItem} pertains to all
	 * 		dimensional indices in {@code dims}.
	 */
	default boolean isAttachedTo(final int... dims) { return false; }

	T get();

	/**
	 *
	 * @param pos
	 * @return
	 */
	default T getAt(RealLocalizable pos) {
		return get();
	}

	default T getAt(long... pos) {
		return getAt(new Point(pos));
	}

	default T getAt(double... pos) {
		return getAt(new RealPoint(pos));
	}
}
