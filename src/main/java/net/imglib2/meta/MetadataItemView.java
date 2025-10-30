package net.imglib2.meta;

import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccessible;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.ViewTransforms;
import net.imglib2.view.fluent.RandomAccessibleView;

import java.util.function.Supplier;

public class MetadataItemView<T> extends MixedTransformView<T> implements  MetadataItem<T>, RandomAccessibleView<T, MetadataItemView<T>> {
    private final MetadataItem<T> source;
    private final MixedTransform transform;

    public MetadataItemView(MetadataItem<T> source) {
        // Use an identity transform
        this(source, new MixedTransform(source.numDimensions(), source.numDimensions()));
    }

    public MetadataItemView(MetadataItem<T> source, Mixed transform) {
        super(source, transform);
        this.source = source;
        this.transform = new MixedTransform(transform.numSourceDimensions(), transform.numTargetDimensions());
        this.transform.set(transform);
    }

    public MetadataItemView(MetadataItemView<T> source, MixedTransform transform) {
        this(source.source, source.transform.concatenate(transform));
    }

    @Override
    public String name() {
        return source.name();
    }

    @Override
    public int[] attachedAxes() {
        // Transform source attached axes to view space
        int[] sourceAttached = source.attachedAxes();

        // Map from source dimension to target dimension using inverse mapping
        // If a source dimension is sliced (not present in target), it should not appear in result
        int[] targetAttached = new int[sourceAttached.length];
        int count = 0;

        for(int i = 0; i < targetAttached.length; i++) {
            int attachedDim = sourceAttached[i];
            if (transform.getComponentZero(attachedDim)) {
                continue; // This target dimension is sliced out
            }
            targetAttached[i] = transform.getComponentMapping(attachedDim);
            count++;
        }
        int[] result = new int[count];
        System.arraycopy(targetAttached, 0, result, 0, count);
        return result;
    }

    @Override
    public int[] varyingAxes() {
        // Transform source attached axes to view space
        int[] sourceVarying = source.varyingAxes();

        // Map from source dimension to target dimension using inverse mapping
        // If a source dimension is sliced (not present in target), it should not appear in result
        int[] targetVarying = new int[sourceVarying.length];
        int count = 0;

        for(int i = 0; i < targetVarying.length; i++) {
            int attachedDim = sourceVarying[i];
            if (transform.getComponentZero(attachedDim)) {
                continue; // This target dimension is sliced out
            }
            targetVarying[i] = transform.getComponentMapping(attachedDim);
            count++;
        }
        int[] result = new int[count];
        System.arraycopy(targetVarying, 0, result, 0, count);
        return result;
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

    // -- RandomAccessibleView fluent methods returning transformed MetadataItemView -- //

    @Override
    public MetadataItemView<T> slice(int d, long pos) {
        return new MetadataItemView<>(this, ViewTransforms.hyperSlice(numDimensions(), d, pos));
    }

    @Override
    public MetadataItemView<T> addDimension() {
        return new MetadataItemView<>(this, ViewTransforms.addDimension(numDimensions()));
    }

    @Override
    public MetadataItemView<T> translate(long... translation) {
        return new MetadataItemView<>(this, ViewTransforms.translate(translation));
    }

    @Override
    public MetadataItemView<T> translateInverse(long... translation) {
        return new MetadataItemView<>(this, ViewTransforms.translateInverse(translation));
    }

    @Override
    public MetadataItemView<T> rotate(int fromAxis, int toAxis) {
        return new MetadataItemView<>(this, ViewTransforms.rotate(numDimensions(), fromAxis, toAxis));
    }

    @Override
    public MetadataItemView<T> permute(int fromAxis, int toAxis) {
        return new MetadataItemView<>(this, ViewTransforms.permute(numDimensions(), fromAxis, toAxis));
    }

    @Override
    public MetadataItemView<T> moveAxis(int fromAxis, int toAxis) {
        return new MetadataItemView<>(this, ViewTransforms.moveAxis(numDimensions(), fromAxis, toAxis));
    }

    @Override
    public MetadataItemView<T> invertAxis(int axis) {
        return new MetadataItemView<>(this, ViewTransforms.invertAxis(numDimensions(), axis));
    }
}
