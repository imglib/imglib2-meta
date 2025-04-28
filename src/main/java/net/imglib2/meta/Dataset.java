package net.imglib2.meta;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.transform.integer.Mixed;
import net.imglib2.transform.integer.MixedTransform;
import net.imglib2.util.Util;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.ViewTransforms;
import net.imglib2.view.Views;
import net.imglib2.view.fluent.RandomAccessibleIntervalView;
import net.imglib2.view.fluent.RandomAccessibleView;
import net.imglib2.view.fluent.RealRandomAccessibleView;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Dataset<T> extends RandomAccessibleView<T, Dataset<T>> {
	RandomAccessible<T> data();
	MetadataStore store();

	@Override
	default RandomAccessible<T> delegate() {
		return data();
	}

	static <T> Dataset<T> wrap(RandomAccessible<T> delegate, MetadataStore store) {
		return new Dataset<T>() {
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

	default RandomAccessibleIntervalView<T> interval(Interval interval) {
		return RandomAccessibleIntervalView.wrap(Views.interval(this.delegate(), interval));
	}

	@Override
	default Dataset<T> slice(int d, long pos) {
		MixedTransformView<T> raView = Views.hyperSlice(this.delegate(), d, pos);
		MetadataStore storeView = new MetadataStoreView(
			store(),
			ViewTransforms.hyperSlice(this.numDimensions(), d, pos)
		);
		return wrap(raView, storeView);
	}

	@Override
	default Dataset<T> addDimension() {
		return wrap(Views.addDimension(this.delegate()), store());
	}

	@Override
	default Dataset<T> translate(long... translation) {
		MixedTransformView<T> raView = Views.translate(this.delegate(), translation);
		MetadataStore storeView = new MetadataStoreView(store(), raView.getTransformToSource());
		return wrap(raView, storeView);
	}

	@Override
	default Dataset<T> translateInverse(long... translation) {
		return wrap(Views.translateInverse(this.delegate(), translation), store());
	}

	@Override
	default Dataset<T> subsample(long... steps) {
		return wrap(Views.subsample(this.delegate(), Util.expandArray(steps, this.numDimensions())), store());
	}

	@Override
	default Dataset<T> rotate(int fromAxis, int toAxis) {
		MixedTransformView<T> raView = Views.rotate(this.delegate(), fromAxis, toAxis);
		MetadataStore storeView = new MetadataStoreView(
				store(),
				ViewTransforms.rotate(this.numDimensions(), fromAxis, toAxis)
		);
		return wrap(raView, storeView);
	}

	@Override
	default Dataset<T> permute(int fromAxis, int toAxis) {
		MixedTransformView<T> raView = Views.permute(this.delegate(), fromAxis, toAxis);
		MetadataStore storeView = new MetadataStoreView(
				store(),
				ViewTransforms.permute(this.numDimensions(), fromAxis, toAxis)
		);
		return wrap(raView, storeView);
	}

	@Override
	default Dataset<T> moveAxis(int fromAxis, int toAxis) {
		return wrap(Views.moveAxis(this.delegate(), fromAxis, toAxis), store());
	}

	@Override
	default Dataset<T> invertAxis(int axis) {
		return wrap(Views.invertAxis(this.delegate(), axis), store());
	}

	default RealRandomAccessibleView<T> interpolate(RandomAccessibleView.Interpolation<T> interpolation) {
		throw new UnsupportedOperationException("TODO");
//		return wrap(RandomAccessibleView.super.interpolate(interpolation));
	}

	default <U> RandomAccessibleView<U, ?> convert(Supplier<U> targetSupplier, Converter<? super T, ? super U> converter) {
		return wrap(Converters.convert2(this.delegate(), converter, targetSupplier), store());
	}

	default <U> RandomAccessibleView<U, ?> convert(Supplier<U> targetSupplier, Supplier<Converter<? super T, ? super U>> converterSupplier) {
		return wrap(Converters.convert2(this.delegate(), converterSupplier, targetSupplier), store());
	}

	default <U> U use(Function<? super Dataset<T>, U> function) {
		return function.apply(this);
	}

	default Dataset<T> view() {
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
