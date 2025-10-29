package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.fluent.RandomAccessibleView;

import java.util.function.Supplier;

public class MetadataItemView<T> extends MixedTransformView<T> implements MetadataItem<T>, RandomAccessibleView<T, MetadataItemView<T>> {
    private final MetadataItem<T> source;
    private final Mixed transform;

    public MetadataItemView(MetadataItem<T> source) {
        // Use an identity transform
        this(source, new MixedTransform(source.numDimensions(), source.numDimensions()));
    }

    public MetadataItemView(MetadataItem<T> source, Mixed transform) {
        super(source, transform);
        this.source = source;
        this.transform = transform;
    }

    @Override
    public String name() {
        return source.name();
    }

    @Override
    public boolean isAttachedTo(int... dims) {
        int[] dd = new int[dims.length];
        for (int i = 0; i < dims.length; i++) {
            dd[i] = transform.getComponentMapping(dims[i]);
        }
        return source.isAttachedTo(dd);
    }

    @Override
    public T getAt(Localizable pos) {
        final Point p = new Point(transform.numSourceDimensions());
        transform.apply(pos, p);
        return source.getAt(p);
    }

    @Override
    public T valueOr(T defaultValue) {
        return source.valueOr(defaultValue);
    }

    @Override
    public MetadataItem<T> or(Supplier<MetadataItem<T>> defaultSupplier) {
        // If the source is present, sourceOr will just return itself
        MetadataItem<T> sourceOr = source.or(defaultSupplier);
        if (sourceOr == source) {
            // The source is not absent
            return this;
        } else {
            // The source is absent
            return sourceOr;
        }
    }

@Override
public RandomAccessible<T> delegate() {
    return source;
}

@Override
public MetadataItemView<T> view() {
    return this;
}
}
