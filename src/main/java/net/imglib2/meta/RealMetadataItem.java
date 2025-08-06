package net.imglib2.meta;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RealLocalizable;
import net.imglib2.RealRandomAccessible;

public interface RealMetadataItem<T> extends MetadataItem<T>, RealRandomAccessible<T> {
    @Override
    T getAt(RealLocalizable pos);

    @Override
    default RandomAccess<T> randomAccess(final Interval interval) {
        return RealRandomAccessible.super.randomAccess();
    }

    @Override
    default T getAt(double... pos) {
        return MetadataItem.super.getAt(pos);
    }
}
