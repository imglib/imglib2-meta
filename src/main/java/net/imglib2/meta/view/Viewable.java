package net.imglib2.meta.view;

import net.imglib2.transform.integer.Mixed;

/**
 * An interface for metadata items that can produce a view of themselves.
 *
 * @param <T>
 * @author Gabriel Selzer
 */
public interface Viewable<T> {

    /**
     * Creates a copy of this metadata with the given transform applied.
     * @param transform the {@link Mixed} to apply
     * @return a transformed version of this metadata item
     */
    T transform(Mixed transform);

    /**
     * Creates a copy of this metadata with the given subsampling applied.
     * @param steps the subsampling steps to apply
     * @return a transformed version of this metadata item
     */
    T transform(long[] steps);
}
