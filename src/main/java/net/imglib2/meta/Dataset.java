package net.imglib2.meta;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.util.Util;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.SubsampleView;
import net.imglib2.view.ViewTransforms;
import net.imglib2.view.Views;
import net.imglib2.view.fluent.RandomAccessibleIntervalView;
import net.imglib2.view.fluent.RandomAccessibleView;
import net.imglib2.view.fluent.RealRandomAccessibleView;

import java.util.function.Supplier;

public interface Dataset<T, V extends Dataset<T, V>> extends RandomAccessibleView<T, V> {
	RandomAccessible<T> data();
	MetadataStore store();

	@Override
	default RandomAccessible<T> delegate() {
		return data();
	}

	// NOTE: The V type variable used for anonymous inner class
	static <T, V extends Dataset<T, V>> Dataset<T, ?> wrap(RandomAccessible<T> delegate, MetadataStore store) {
		return new Dataset<T, V>() {
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

	static <T> Dataset<T, ?> wrap(Dataset<T, ?> dataset, Mixed tform) {
		return wrap(
			new MixedTransformView<>(dataset.delegate(), tform),
			new MetadataStoreView(dataset.store(), tform)
		);
	}

	// FIXME: Return some Dataset subclass
	default DatasetInterval<T, ?> interval(Interval interval) {
		return DatasetInterval.wrap(Views.interval(this.delegate(), interval), store());
	}

	@Override
	default Dataset<T, ?> slice(int d, long pos) {
		return wrap( this, ViewTransforms.hyperSlice(numDimensions(), d, pos) );
	}

	@Override
	default Dataset<T, ?> addDimension() {
		return wrap( this, ViewTransforms.addDimension(numDimensions()) );
	}

	@Override
	default Dataset<T, ?> translate(long... translation) {
		return wrap(this, ViewTransforms.translate(translation) );
	}

	@Override
	default Dataset<T, ?> translateInverse(long... translation) {
		return wrap(this, ViewTransforms.translateInverse(translation) );
	}

	@Override
	default Dataset<T, ?> subsample(long... steps) {
		long[] fullSteps = Util.expandArray(steps, this.numDimensions());
		SubsampleView<T> dataView = Views.subsample(this.delegate(), fullSteps);
		MetadataStore storeView = new MetadataStoreSubsampleView(
			store(),
			fullSteps
		);
		return wrap(dataView, storeView);
	}

	@Override
	default Dataset<T, ?> rotate(int fromAxis, int toAxis) {
		return wrap( this, ViewTransforms.rotate(numDimensions(), fromAxis, toAxis) );
	}

	@Override
	default Dataset<T, ?> permute(int fromAxis, int toAxis) {
		return wrap( this, ViewTransforms.permute(numDimensions(), fromAxis, toAxis) );
	}

	@Override
	default Dataset<T, ?> moveAxis(int fromAxis, int toAxis) {
		return wrap( this, ViewTransforms.moveAxis(numDimensions(), fromAxis, toAxis) );
	}

	@Override
	default Dataset<T, ?> invertAxis(int axis) {
		return wrap( this, ViewTransforms.invertAxis(numDimensions(), axis) );
	}

	default RealRandomAccessibleView<T> interpolate(RandomAccessibleView.Interpolation<T> interpolation) {
		throw new UnsupportedOperationException("TODO");
//		return wrap(RandomAccessibleView.super.interpolate(interpolation));
	}

	// FIXME: Dataset wildcard bound
	default <U> Dataset<U, ?> convert(Supplier<U> targetSupplier, Converter<? super T, ? super U> converter) {
		return wrap(Converters.convert2(this.delegate(), converter, targetSupplier), store());
	}

	// FIXME: Dataset wildcard bound
	default <U> Dataset<U, ?> convert(Supplier<U> targetSupplier, Supplier<Converter<? super T, ? super U>> converterSupplier) {
		return wrap(Converters.convert2(this.delegate(), converterSupplier, targetSupplier), store());
	}

	default Dataset<T, V> view() {
		return this;
	}

	default T getType() {
		return this.delegate().getType();
	}

	default int numDimensions() {
		return this.delegate().numDimensions();
	}

	default RandomAccess<T> randomAccess() {
		return this.delegate().randomAccess();
	}

	default RandomAccess<T> randomAccess(Interval interval) {
		return this.delegate().randomAccess(interval);
	}

}
