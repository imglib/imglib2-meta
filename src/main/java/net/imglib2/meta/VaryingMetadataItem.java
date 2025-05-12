package net.imglib2.meta;

import net.imglib2.Point;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;

/**
 * A {@link MetadataItem} that varies along at least one axis.
 * @param <T> the type of the value along each axis
 * @param <F> the type of the container that holds the elements.
 * @author Gabriel Selzer
 */
public interface VaryingMetadataItem<T, F> extends MetadataItem<F> {

    @Override
    default boolean isAttachedToAxes() { return true; }

    T getAt(RealLocalizable pos);

    default T getAt(long... pos) {
        return getAt(new Point(pos));
    }

    default T getAt(double... pos) {
        return getAt(new RealPoint(pos));
    }

}
