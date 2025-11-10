package net.imglib2.meta.view;

import net.imglib2.transform.integer.Mixed;

/**
 * An interface for metadata items that can produce a view of themselves.
 *
 * @param <T>
 * @author Gabriel Selzer
 */
public interface Viewable<T> {


    T transform(Mixed transform, int... srcAxes);

    T transform(long[] steps, int... attachedAxes);
}
