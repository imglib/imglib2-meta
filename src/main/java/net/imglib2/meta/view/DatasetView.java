package net.imglib2.meta.view;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.meta.Dataset;
import net.imglib2.meta.MetadataStore;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.util.Util;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.SubsampleView;
import net.imglib2.view.ViewTransforms;
import net.imglib2.view.Views;
import net.imglib2.view.fluent.RandomAccessibleView;
import net.imglib2.view.fluent.RealRandomAccessibleView;

import java.util.function.Supplier;

/**
 * A view on a {@link Dataset}.
 *
 * @author Gabriel Selzer
 * @param <T> the type of samples in the {@link RandomAccessible}
 * @param <V> the concrete subtype of {@link DatasetView}
 */
public interface DatasetView<T, V extends DatasetView<T, V>> extends RandomAccessibleView<T, V>, Dataset<T> {

    /**
     * Creates a new {@link DatasetView} from a {@link RandomAccessible} and a {@link MetadataStore}.
     *
     * @param delegate the coupled {@link RandomAccessible}
     * @param store the coupled {@link MetadataStore}
     * @return a {@link Dataset} wrapping {@code delegate} and {@code store}
     * @param <T> the type of pixels contained within {@code delegate}
     */
    static <T, V extends DatasetView<T, V>> DatasetView<T, ?> wrap(RandomAccessible<T> delegate, MetadataStore store) {
        return new DatasetView<T, V>() {

            @Override
            public RandomAccessible<T> delegate() {
                return delegate;
            }

            @Override
            public RandomAccessible<T> data() {
                return delegate;
            }

            @Override
            public MetadataStore store() {
                return store;
            }
        };
    }

    /**
     * Creates a new {@link DatasetView} viewing an existing {@link Dataset} through a {@link Mixed} transform.
     *
     * @param dataset an existing {@link Dataset}
     * @param tform a {@link Mixed} describing a data transformation
     * @return a {@link Dataset} viewing {@code dataset} through the transform {@code tform}
     * @param <T> the type of pixels contained within {@code delegate}
     */
    static <T> DatasetView<T, ?> wrap(Dataset<T> dataset, Mixed tform) {
        return wrap(
                new MixedTransformView<>(dataset.data(), tform),
                new MetadataStoreView(dataset.store(), tform)
        );
    }


    @Override
    default DatasetIntervalView<T, ?> interval(Interval interval) {
        return DatasetIntervalView.wrap(Views.interval(this.data(), interval), store());
    }

    @Override
    default DatasetView<T, ?> slice(int d, long pos) {
        return wrap( this, ViewTransforms.hyperSlice(numDimensions(), d, pos) );
    }

    @Override
    default DatasetView<T, ?> addDimension() {
        return wrap( this, ViewTransforms.addDimension(numDimensions()) );
    }

    @Override
    default DatasetView<T, ?> translate(long... translation) {
        return wrap(this, ViewTransforms.translate(translation) );
    }

    @Override
    default DatasetView<T, ?> translateInverse(long... translation) {
        return wrap(this, ViewTransforms.translateInverse(translation) );
    }

    @Override
    default DatasetView<T, ?> subsample(long... steps) {
        long[] fullSteps = Util.expandArray(steps, this.numDimensions());
        SubsampleView<T> dataView = Views.subsample(this.delegate(), fullSteps);
        MetadataStore storeView = new MetadataStoreSubsampleView(
                store(),
                fullSteps
        );
        return wrap(dataView, storeView);
    }

    @Override
    default DatasetView<T, ?> rotate(int fromAxis, int toAxis) {
        return wrap( this, ViewTransforms.rotate(numDimensions(), fromAxis, toAxis) );
    }

    @Override
    default DatasetView<T, ?> permute(int fromAxis, int toAxis) {
        return wrap( this, ViewTransforms.permute(numDimensions(), fromAxis, toAxis) );
    }

    @Override
    default DatasetView<T, ?> moveAxis(int fromAxis, int toAxis) {
        return wrap( this, ViewTransforms.moveAxis(numDimensions(), fromAxis, toAxis) );
    }

    @Override
    default DatasetView<T, ?> invertAxis(int axis) {
        return wrap( this, ViewTransforms.invertAxis(numDimensions(), axis) );
    }

    @Override
    default RealRandomAccessibleView<T, ?> interpolate(RandomAccessibleView.Interpolation<T> interpolation) {
        // TODO: Return a RealDataset
        return RandomAccessibleView.super.interpolate(interpolation);
    }

    @Override
    default <U> DatasetView<U, ?> convert(Supplier<U> targetSupplier, Converter<? super T, ? super U> converter) {
        return wrap(Converters.convert2(this.delegate(), converter, targetSupplier), store());
    }

    @Override
    default <U> DatasetView<U, ?> convert(Supplier<U> targetSupplier, Supplier<Converter<? super T, ? super U>> converterSupplier) {
        return wrap(Converters.convert2(this.delegate(), converterSupplier, targetSupplier), store());
    }

    @Override
    default DatasetView<T, ?> view() {
        return this;
    }

    @Override
    default T getType() {
        return this.data().getType();
    }

    @Override
    default int numDimensions() {
        return this.data().numDimensions();
    }

    @Override
    default RandomAccess<T> randomAccess() {
        return this.data().randomAccess();
    }

    @Override
    default RandomAccess<T> randomAccess(Interval interval) {
        return this.data().randomAccess(interval);
    }

}
